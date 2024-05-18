package com.tf4.photospot.global.exception.domain;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import com.tf4.photospot.global.exception.ApiErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements ApiErrorCode {
	NOT_FOUND_TAG(HttpStatus.NOT_FOUND, "존재하지 않는 태그입니다."),
	NOT_FOUND_POST(HttpStatus.NOT_FOUND, "존재하지 않는 방명록입니다."),
	ALREADY_LIKE(HttpStatus.BAD_REQUEST, "이미 해당 방명록을 좋아요 했습니다."),
	NO_EXISTS_LIKE(HttpStatus.BAD_REQUEST, "해당 방명록 좋아요가 존재하지 않습니다."),
	ALREADY_REPORT(HttpStatus.CONFLICT, "해당 방명록을 이미 신고하였습니다."),
	CAN_NOT_REPORT_OWN_POST(HttpStatus.FORBIDDEN, "본인이 작성한 방명록은 신고할 수 없습니다."),
	CAN_NOT_DELETE_POSTS(HttpStatus.BAD_REQUEST, "방명록 목록 삭제를 할 수 없습니다.");

	private final HttpStatusCode statusCode;
	private final String message;
}
