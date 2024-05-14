package com.tf4.photospot.global.exception.domain;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import com.tf4.photospot.global.exception.ApiErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpotErrorCode implements ApiErrorCode {
	INVALID_SPOT_ID(HttpStatus.BAD_REQUEST, "유효하지 않는 SPOT ID입니다."),
	INVALID_PERIOD(HttpStatus.BAD_REQUEST, "시작일이 종료일 이전이어야 합니다."),
	EXCEEDED_PERIOD(HttpStatus.BAD_REQUEST, "선택한 기간이 7일을 초과합니다.");

	private final HttpStatusCode statusCode;
	private final String message;
}
