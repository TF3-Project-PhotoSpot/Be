package com.tf4.photospot.user.presentation.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NicknameUpdateRequest(
	@Pattern(regexp = "^[0-9a-zA-Zㄱ-ㅎ가-힣._]*$", message = "닉네임은 문자, 숫자, 밑줄 및 마침표만 사용할 수 있습니다.")
	@Size(max = 20, message = "닉네임은 최대 15자까지 입력할 수 있습니다.")
	String nickname
) {
}
