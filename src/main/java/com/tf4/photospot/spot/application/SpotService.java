package com.tf4.photospot.spot.application;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.SpotErrorCode;
import com.tf4.photospot.post.application.request.PostSearchCondition;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.post.application.response.RecentPostPreviewResponse;
import com.tf4.photospot.post.infrastructure.PostJdbcRepository;
import com.tf4.photospot.post.infrastructure.PostQueryRepository;
import com.tf4.photospot.spot.application.request.NearbySpotRequest;
import com.tf4.photospot.spot.application.request.RecommendedSpotsRequest;
import com.tf4.photospot.spot.application.response.MostPostTagRank;
import com.tf4.photospot.spot.application.response.NearbySpotListResponse;
import com.tf4.photospot.spot.application.response.PeriodPostResponse;
import com.tf4.photospot.spot.application.response.PeriodSpotResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotListResponse;
import com.tf4.photospot.spot.application.response.SpotCoordResponse;
import com.tf4.photospot.spot.application.response.SpotResponse;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.spot.domain.SpotRepository;
import com.tf4.photospot.spot.infrastructure.SpotQueryRepository;
import com.tf4.photospot.spot.presentation.request.DateDto;
import com.tf4.photospot.user.application.UserService;
import com.tf4.photospot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SpotService {
	private final SpotRepository spotRepository;
	private final SpotQueryRepository spotQueryRepository;
	private final PostJdbcRepository postJdbcRepository;
	private final PostQueryRepository postQueryRepository;
	private final UserService userService;

	/*
	 *	특정 좌표의 반경 내 추천 스팟들의 최신 방명록 미리보기를 조회합니다.
	 * 	추천 스팟은 방명록이 많은 순으로 정렬 됩니다.
	 * */
	public RecommendedSpotListResponse getRecommendedSpotList(RecommendedSpotsRequest request) {
		Slice<Spot> recommendedSpots = spotQueryRepository.searchRecommendedSpots(request.coord(),
			request.radius(), request.pageable());
		if (recommendedSpots.isEmpty()) {
			return RecommendedSpotListResponse.emptyResponse();
		}
		final List<RecentPostPreviewResponse> postPreviews = getRecentPostPreviewsInSpots(recommendedSpots.getContent(),
			request.postPreviewCount());
		return RecommendedSpotListResponse.of(recommendedSpots, postPreviews);
	}

	public NearbySpotListResponse getNearbySpotList(NearbySpotRequest request) {
		return new NearbySpotListResponse(spotQueryRepository.findNearbySpots(request.coord(), request.radius()));
	}

	public SpotResponse findSpot(PostSearchCondition searchCond, int mostPostTagCount) {
		final Spot spot = spotRepository.findById(searchCond.spotId())
			.orElseThrow(() -> new ApiException(SpotErrorCode.INVALID_SPOT_ID));
		final Slice<PostPreviewResponse> postPreviews = postQueryRepository.findPostPreviews(searchCond);
		final List<MostPostTagRank> mostPostTagRanks = postJdbcRepository.findMostPostTagsOfSpot(spot,
			mostPostTagCount);
		return SpotResponse.of(spot, mostPostTagRanks, postPreviews.getContent());
	}

	public List<SpotCoordResponse> findSpotsOfMyPosts(Long userId) {
		return spotQueryRepository.findSpotsOfMyPosts(userId);
	}

	public Spot getSpot(Long spotId) {
		return spotRepository.findById(spotId)
			.orElseThrow(() -> new ApiException(SpotErrorCode.INVALID_SPOT_ID));
	}

	public List<RecentPostPreviewResponse> getRecentPostPreviewsInSpots(List<Spot> spots, int postPreviewCount) {
		return postJdbcRepository.findRecentPostPreviewsInSpots(spots, postPreviewCount);
	}

	public List<PeriodSpotResponse> findSpotsOfMyPostsWithinPeriod(Long userId, DateDto dateDto) {
		User user = userService.getActiveUser(userId);
		final List<PeriodPostResponse> posts = postQueryRepository.findSpotsWithinPeriod(user.getId(), dateDto.start(),
			dateDto.end());
		final Map<Long, List<PeriodPostResponse>> postsGroupBySpot = posts.stream()
			.collect(Collectors.groupingBy(PeriodPostResponse::spotId));
		return postsGroupBySpot.keySet().stream()
			.map(spotId -> PeriodSpotResponse.from(postsGroupBySpot.get(spotId)))
			.toList();
	}
}
