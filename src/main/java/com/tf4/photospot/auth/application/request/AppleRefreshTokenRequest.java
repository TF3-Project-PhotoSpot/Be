package com.tf4.photospot.auth.application.request;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppleRefreshTokenRequest {
	private String clientId;
	private String clientSecret;
	private String code;

	public AppleRefreshTokenRequest(String clientId, String clientSecret, String code) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.code = code;
	}

	public MultiValueMap<String, String> toMultiValueMap() {
		MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
		request.add("client_id", clientId);
		request.add("client_secret", clientSecret);
		request.add("code", code);
		request.add("grant_type", "authorization_code");
		return request;
	}
}
