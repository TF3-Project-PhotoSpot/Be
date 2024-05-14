package com.tf4.photospot.auth.application.request;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppleRevokeRequest {
	private String clientId;
	private String clientSecret;
	private String token;

	public AppleRevokeRequest(String clientId, String clientSecret, String token) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.token = token;
	}

	public MultiValueMap<String, String> toMultiValueMap() {
		MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
		request.add("client_id", clientId);
		request.add("client_secret", clientSecret);
		request.add("token", token);
		return request;
	}
}
