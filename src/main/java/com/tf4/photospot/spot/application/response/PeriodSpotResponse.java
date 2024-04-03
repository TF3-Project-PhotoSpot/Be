package com.tf4.photospot.spot.application.response;

import java.util.List;

import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.global.util.PointConverter;

public record PeriodSpotResponse(
	Long spotId,
	CoordinateDto coord,
	List<IncludedPostResponse> posts
) {
	public static PeriodSpotResponse from(List<PeriodPostResponse> responses) {
		return new PeriodSpotResponse(
			responses.get(0).spotId(),
			PointConverter.convert(responses.get(0).coord()),
			responses.stream()
				.map(IncludedPostResponse::from)
				.toList());
	}
}
