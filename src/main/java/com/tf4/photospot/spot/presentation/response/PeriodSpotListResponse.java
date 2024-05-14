package com.tf4.photospot.spot.presentation.response;

import java.util.List;

import com.tf4.photospot.spot.application.response.PeriodSpotResponse;

public record PeriodSpotListResponse(
	List<PeriodSpotResponse> spots
) {
}
