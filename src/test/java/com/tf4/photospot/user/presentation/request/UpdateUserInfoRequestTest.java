package com.tf4.photospot.user.presentation.request;

import static com.tf4.photospot.support.TestFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

public class UpdateUserInfoRequestTest {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@ParameterizedTest
	@MethodSource("provideValidNickname")
	@DisplayName("유효한 닉네임 검증에 성공한다.")
	void successValidUpdateUserInfoRequest(String nickname) {
		NicknameUpdateRequest request = new NicknameUpdateRequest(nickname);
		Set<ConstraintViolation<NicknameUpdateRequest>> violations = validator.validate(request);
		assertTrue(violations.isEmpty());
	}

	@ParameterizedTest
	@MethodSource("provideInvalidNickname")
	@DisplayName("유효하지 않은 문자가 포함된 닉네임 검증에 성공한다.")
	void failUpdateUserInfoRequestWithInvalidCharacter(String nickname) {
		NicknameUpdateRequest request = new NicknameUpdateRequest(nickname);
		Set<ConstraintViolation<NicknameUpdateRequest>> violations = validator.validate(request);
		assertThat(violations.stream().findFirst().map(ConstraintViolation::getMessage).orElseThrow())
			.isEqualTo("닉네임은 문자, 숫자, 밑줄 및 마침표만 사용할 수 있습니다.");
	}

	@ParameterizedTest
	@MethodSource("provideExceedNickname")
	@DisplayName("글자수 20자 넘는 닉네임 검증에 성공한다.")
	void failUpdateUserInfoRequestWithExceedSize(String nickname) {
		NicknameUpdateRequest request = new NicknameUpdateRequest(nickname);
		Set<ConstraintViolation<NicknameUpdateRequest>> violations = validator.validate(request);
		assertThat(violations.stream().findFirst().map(ConstraintViolation::getMessage).orElseThrow())
			.isEqualTo("닉네임은 최대 15자까지 입력할 수 있습니다.");
	}

	private static Stream<Arguments> provideValidNickname() {
		return Stream.of(Arguments.of("한글닉네임", "engNickname", "number123", "허용_가능.닉네임"));
	}

	private static Stream<Arguments> provideInvalidNickname() {
		return Stream.of(Arguments.of("불가능한/닉네임", "wrong*nickname", "fail 123", "틀린-닉네임"));
	}

	private static Stream<Arguments> provideExceedNickname() {
		String nickname = createDummyStr(21);
		return Stream.of(Arguments.of(nickname));
	}
}
