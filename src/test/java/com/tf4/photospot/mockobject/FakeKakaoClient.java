package com.tf4.photospot.mockobject;

import com.tf4.photospot.auth.application.response.KakaoTokenInfoResponse;
import com.tf4.photospot.auth.application.response.KakaoUnlinkResponse;
import com.tf4.photospot.auth.infrastructure.KakaoClient;

public class FakeKakaoClient implements KakaoClient {
	@Override
	public KakaoTokenInfoResponse getTokenInfo(String accessToken) {
		var response = new KakaoTokenInfoResponse();
		response.setAccount(456789L);
		response.setExpiresIn(7199);
		response.setAppId(123456);
		return response;
	}

	@Override
	public KakaoUnlinkResponse unlink(String adminKey, String targetIdType, String targetId) {
		var response = new KakaoUnlinkResponse();
		response.setAccount(123456789L);
		return response;
	}
}
