package com.tf4.photospot.spot.presentation.response;

import java.util.List;

import com.tf4.photospot.spot.application.response.RecommendedSpotListResponse;

import io.micrometer.common.util.StringUtils;
import lombok.Builder;

@Builder
public record RecommendedSpotListHttpResponse(
	String centerAddress,
	List<RecommendedSpotHttpResponse> recommendedSpots,
	Boolean hasNext
) {
	private static final String CAN_NOT_FOUND_ADDRESS = "현재 위치를 찾을 수 없습니다.";

	public RecommendedSpotListHttpResponse {
		if (StringUtils.isEmpty(centerAddress)) {
			centerAddress = recommendedSpots.stream()
				.map(RecommendedSpotHttpResponse::address)
				.filter(StringUtils::isNotEmpty)
				.findFirst()
				.orElseGet(() -> CAN_NOT_FOUND_ADDRESS);
		}
	}

	public static RecommendedSpotListHttpResponse of(String centerAddress, RecommendedSpotListResponse response) {
		return RecommendedSpotListHttpResponse.builder()
			.centerAddress(centerAddress)
			.recommendedSpots(RecommendedSpotHttpResponse.convert(response.recommendedSpots()))
			.hasNext(response.hasNext())
			.build();
	}
}
