package com.tf4.photospot.global.argument;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.tf4.photospot.spot.presentation.request.DateDto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

public class DateValidatorTest {

	private Validator validator;

	@BeforeEach
	public void setUp() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Test
	@DisplayName("유효한 날짜 기간이 들어오면 Validator를 통과한다.")
	void dateValidSuccessTest() {
		DateDto dateDto = new DateDto(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5));
		Set<ConstraintViolation<DateDto>> violations = validator.validate(dateDto);
		assertThat(violations).isEmpty();
	}

	@MethodSource(value = "getDateTestData")
	@ParameterizedTest(name = "{0}, validErrors {1}")
	@DisplayName("빈 값 또는 유효하지 않는 날짜가 들어오면 ConstraintViolation가 추가된다.")
	void dateValidFailTest(DateDto dateDto, Tuple... validErrors) {
		String fieldPath = "propertyPath.currentLeafNode.name";
		String errorMessagePath = "messageTemplate";
		Set<ConstraintViolation<DateDto>> violations = validator.validate(dateDto);
		assertThat(violations)
			.extracting(fieldPath, errorMessagePath)
			.contains(validErrors);
	}

	private static Stream<Arguments> getDateTestData() {
		return Stream.of(
			Arguments.of(new DateDto(null, LocalDate.of(2024, 1, 5)), new Tuple[] {
				new Tuple("start", DateValidator.DATE_NOT_EMPTY)
			}),
			Arguments.of(new DateDto(LocalDate.of(2024, 1, 1), null), new Tuple[] {
				new Tuple("end", DateValidator.DATE_NOT_EMPTY)
			}),
			Arguments.of(new DateDto(LocalDate.of(2024, 1, 7), LocalDate.of(2024, 1, 5)), new Tuple[] {
				new Tuple("start", DateValidator.INVALID_PERIOD)
			}),
			Arguments.of(new DateDto(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 8)), new Tuple[] {
				new Tuple("end", DateValidator.EXCEEDED_PERIOD)
			})
		);
	}
}
