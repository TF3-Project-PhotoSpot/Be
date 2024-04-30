package com.tf4.photospot.map.application.response.kakao;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = KakaoCoordToAddressDeserializer.class)
public record KakaoCoordToAddressResponse(
	String address
) {
	public static final KakaoCoordToAddressResponse ERROR_RESPONSE = new KakaoCoordToAddressResponse("");
}
