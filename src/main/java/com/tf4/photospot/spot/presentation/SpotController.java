package com.tf4.photospot.spot.presentation;

import java.util.List;

import org.hibernate.validator.constraints.Range;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.bookmark.application.BookmarkService;
import com.tf4.photospot.bookmark.application.response.BookmarkOfSpotResponse;
import com.tf4.photospot.global.argument.AuthUserId;
import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.global.util.PointConverter;
import com.tf4.photospot.map.application.MapService;
import com.tf4.photospot.post.application.request.PostSearchCondition;
import com.tf4.photospot.post.application.request.PostSearchType;
import com.tf4.photospot.spot.application.SpotService;
import com.tf4.photospot.spot.application.request.NearbySpotRequest;
import com.tf4.photospot.spot.application.request.RecommendedSpotsRequest;
import com.tf4.photospot.spot.application.response.NearbySpotListResponse;
import com.tf4.photospot.spot.application.response.SpotResponse;
import com.tf4.photospot.spot.presentation.request.DateDto;
import com.tf4.photospot.spot.presentation.request.FindSpotHttpRequest;
import com.tf4.photospot.spot.presentation.response.PeriodSpotListResponse;
import com.tf4.photospot.spot.presentation.response.RecommendedSpotListHttpResponse;
import com.tf4.photospot.spot.presentation.response.SpotHttpResponse;
import com.tf4.photospot.spot.presentation.response.UserSpotListHttpResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RequestMapping("/api/v1/spots")
@RestController
@RequiredArgsConstructor
public class SpotController {
	private final SpotService spotService;
	private final MapService mapService;
	private final BookmarkService bookmarkService;

	@GetMapping("/recommended")
	public RecommendedSpotListHttpResponse getSpotList(
		@ModelAttribute @Valid CoordinateDto coord,
		@RequestParam(name = "radius") @Positive(message = "반경(m)은 0보다 커야 됩니다.") Integer radius,
		@RequestParam(name = "postPreviewCount", defaultValue = "5")
		@Range(min = 1, max = 10, message = "미리보기 사진은 1~10개만 가능합니다.") Integer postPreviewCount,
		Pageable pageable
	) {
		var request = new RecommendedSpotsRequest(coord.toCoord(), radius, postPreviewCount, pageable);
		return RecommendedSpotListHttpResponse.of(
			mapService.searchByCoord(PointConverter.convert(coord)),
			spotService.getRecommendedSpotList(request)
		);
	}

	@GetMapping
	public NearbySpotListResponse getNearbySpotList(
		@ModelAttribute @Valid CoordinateDto coord,
		@RequestParam(name = "radius") @Positive(message = "반경(m)은 0보다 커야 됩니다.") Integer radius
	) {
		return spotService.getNearbySpotList(new NearbySpotRequest(coord.toCoord(), radius));
	}

	@GetMapping("/{spotId}")
	public SpotHttpResponse getSpot(
		@PathVariable(name = "spotId") Long spotId,
		@ModelAttribute @Valid CoordinateDto startingCoord,
		@ModelAttribute @Valid FindSpotHttpRequest request,
		@AuthUserId Long userId
	) {
		Integer distance = 0;
		Sort sortByLatest = Sort.by(Sort.Direction.DESC, "id");
		PostSearchCondition postSearchCond = PostSearchCondition.builder()
			.spotId(spotId)
			.userId(userId)
			.pageable(PageRequest.of(0, request.postPreviewCount(), sortByLatest))
			.type(PostSearchType.POSTS_OF_SPOT)
			.build();
		final SpotResponse spotResponse = spotService.findSpot(postSearchCond, request.mostPostTagCount());
		final List<BookmarkOfSpotResponse> bookmarksOfSpot = bookmarkService.findBookmarksOfSpot(spotId, userId);
		if (request.requireDistance()) {
			distance = mapService.searchDistanceBetween(startingCoord.toCoord(), spotResponse.coord());
		}
		return SpotHttpResponse.of(distance, spotResponse, bookmarksOfSpot);
	}

	@GetMapping("/mine")
	public UserSpotListHttpResponse getSpotsOfMyPosts(
		@AuthUserId Long userId
	) {
		return UserSpotListHttpResponse.from(spotService.findSpotsOfMyPosts(userId));
	}

	@GetMapping("/mine/period")
	public PeriodSpotListResponse getSpotsOfMyPostsWithinPeriod(
		@AuthUserId Long userId,
		@ModelAttribute @Valid DateDto dateDto
	) {
		return new PeriodSpotListResponse(spotService.findSpotsOfMyPostsWithinPeriod(userId, dateDto));
	}
}
