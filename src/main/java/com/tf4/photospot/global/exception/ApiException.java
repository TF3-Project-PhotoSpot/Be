package com.tf4.photospot.global.exception;

import org.springframework.http.HttpStatusCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApiException extends RuntimeException {
	private final ApiErrorCode errorCode;

	public ApiException(ApiErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
	}

	public HttpStatusCode getStatusCode() {
		return errorCode.getStatusCode();
	}

	public String getMessage() {
		return errorCode.getMessage();
	}

	public String getName() {
		return errorCode.name();
	}
}
