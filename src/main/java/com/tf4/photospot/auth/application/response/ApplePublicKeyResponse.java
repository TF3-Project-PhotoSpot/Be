package com.tf4.photospot.auth.application.response;

import java.util.List;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;

public record ApplePublicKeyResponse(List<ApplePublicKey> keys) {
	public ApplePublicKey getMatchedKey(String kid, String alg) {
		return keys.stream()
			.filter(key -> key.kid().equals(kid) && key.alg().equals(alg))
			.findAny()
			.orElseThrow(() -> new ApiException(AuthErrorCode.INVALID_APPLE_PUBLIC_KEY));
	}
}
