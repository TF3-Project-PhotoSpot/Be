package com.tf4.photospot.global.exception.domain;

import com.tf4.photospot.global.exception.ApiErrorCode;
import com.tf4.photospot.global.exception.ApiException;

import lombok.Getter;

@Getter
public class DetailApiException extends ApiException {
	public DetailApiException(ApiErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}
