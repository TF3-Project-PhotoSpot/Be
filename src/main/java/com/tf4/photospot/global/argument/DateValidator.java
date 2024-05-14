package com.tf4.photospot.global.argument;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.tf4.photospot.spot.presentation.request.DateDto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateValidator implements ConstraintValidator<ValidDate, DateDto> {
	public static final String DATE_NOT_EMPTY = "시작일 또는 종료일에 빈 값이 들어갈 수 없습니다.";
	public static final String INVALID_PERIOD = "시작일은 종료일 이전이어야 합니다.";
	public static final String EXCEEDED_PERIOD = "선택한 기간이 7일을 초과합니다.";

	@Override
	public boolean isValid(DateDto value, ConstraintValidatorContext context) {
		boolean isValid = true;
		final LocalDate start = value.start();
		final LocalDate end = value.end();

		if (start == null || end == null) {
			if (start == null) {
				addConstraintViolationMessage(context, "start", DATE_NOT_EMPTY);
			}
			if (end == null) {
				addConstraintViolationMessage(context, "end", DATE_NOT_EMPTY);
			}
			return false;
		}
		if (start.isAfter(end)) {
			addConstraintViolationMessage(context, "start", INVALID_PERIOD);
			isValid = false;
		}
		if (ChronoUnit.DAYS.between(start, end) + 1 > 7) {
			addConstraintViolationMessage(context, "end", EXCEEDED_PERIOD);
			isValid = false;
		}
		return isValid;
	}

	private void addConstraintViolationMessage(ConstraintValidatorContext context, String field, String message) {
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(message)
			.addPropertyNode(field)
			.addConstraintViolation();
	}
}
