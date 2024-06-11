package com.tf4.photospot.spot.application.response;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Slice;

import com.tf4.photospot.post.application.response.RecentPostPreviewResponse;
import com.tf4.photospot.spot.domain.Spot;

import lombok.Builder;

@Builder
public record RecommendedSpotListResponse(
	List<RecommendedSpotResponse> recommendedSpots,
	Boolean hasNext
) {
	public static RecommendedSpotListResponse emptyResponse() {
		return new RecommendedSpotListResponse(Collections.emptyList(), false);
	}

	public static RecommendedSpotListResponse of(Slice<Spot> spots, List<RecentPostPreviewResponse> postPreviews) {
		Map<Long, List<RecentPostPreviewResponse>> spotPostPreviews = postPreviews.stream()
			.collect(Collectors.groupingBy(RecentPostPreviewResponse::spotId));
		return RecommendedSpotListResponse.builder()
			.recommendedSpots(spots.stream()
				.map(spot -> RecommendedSpotResponse.of(
					spot, spotPostPreviews.getOrDefault(spot.getId(), Collections.emptyList())))
				.toList())
			.hasNext(spots.hasNext())
			.build();
	}
}
