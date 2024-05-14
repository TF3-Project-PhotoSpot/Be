package com.tf4.photospot.post.presentation.request;

import jakarta.validation.constraints.Size;

public record PostReportRequest(
	@Size(max = 200, message = "신고 사유는 200자 이하로 입력해주세요.")
	String reason
) {
}
