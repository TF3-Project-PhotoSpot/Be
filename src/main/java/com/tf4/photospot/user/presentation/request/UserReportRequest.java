package com.tf4.photospot.user.presentation.request;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.constraints.Size;

public record UserReportRequest(
	@Size(max = 200, message = "신고 사유는 200자 이하로 입력해주세요.")
	String reason
) {
	public UserReportRequest {
		if (StringUtils.isEmpty(reason)) {
			reason = "";
		}
	}
}
