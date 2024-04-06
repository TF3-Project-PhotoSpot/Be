package com.tf4.photospot.global.argument;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = DateValidator.class)
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDate {
	String message() default "날짜 형식이 잘못되었습니다.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
