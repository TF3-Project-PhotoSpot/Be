package com.tf4.photospot.map.application.response.kakao;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.tf4.photospot.global.dto.CoordinateDto;

import lombok.Builder;

@Builder
@JsonDeserialize(using = KakaoSearchLocationDeserializer.class)
public record KakaoSearchLocationResponse(
	String address,
	CoordinateDto coord
) {
	public static final KakaoSearchLocationResponse ERROR_RESPONSE = new KakaoSearchLocationResponse(
		"", new CoordinateDto(0d, 0d)
	);
}
