package com.tf4.photospot.mockobject;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.tf4.photospot.auth.infrastructure.KakaoClient;

@TestConfiguration
public class MockKakaoAuthConfig {
	@Bean
	@Primary
	public KakaoClient fakeKakaoClient() {
		return new FakeKakaoClient();
	}
}
