package com.tf4.photospot.map.application.response;

import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.map.application.response.kakao.KakaoSearchLocationResponse;

import lombok.Builder;

@Builder
public record SearchLocationResponse(
	String address,
	CoordinateDto coord
) {
	public static final SearchLocationResponse ERROR_RESPONSE = new SearchLocationResponse(
		"",
		new CoordinateDto(0d, 0d)
	);

	public static final SearchLocationResponse from(KakaoSearchLocationResponse response) {
		if (response == KakaoSearchLocationResponse.ERROR_RESPONSE) {
			return ERROR_RESPONSE;
		}
		return new SearchLocationResponse(
			response.address(),
			response.coord()
		);
	}
}
