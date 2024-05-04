package com.tf4.photospot.global.exception;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.tf4.photospot.global.dto.ApiResponse;
import com.tf4.photospot.global.dto.ValidationError;
import com.tf4.photospot.global.exception.domain.CommonErrorCode;
import com.tf4.photospot.global.exception.domain.DetailApiException;

import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<Object> handleApiException(ApiException ex) {
		return createResponse(ex.getErrorCode());
	}

	@ExceptionHandler(DetailApiException.class)
	public ResponseEntity<Object> handleDetailApiException(DetailApiException ex) {
		log.info("{} DETAILS: ", ex.getMessage(), ex.getCause());
		return createResponse(ex.getErrorCode());
	}

	@Override
	protected ResponseEntity<Object> handleHandlerMethodValidationException(
		@NotNull HandlerMethodValidationException ex,
		@Nullable HttpHeaders headers,
		@NotNull HttpStatusCode status,
		@NotNull WebRequest request
	) {
		final var errorMessages = ex.getAllValidationResults().stream()
			.map(ParameterValidationResult::getResolvableErrors)
			.flatMap(Collection::stream)
			.map(error -> ValidationError.builder().message(error.getDefaultMessage()).build())
			.toList();
		return createResponse(CommonErrorCode.INVALID_PARAMETER, errorMessages);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
		MethodArgumentNotValidException ex,
		@NotNull HttpHeaders headers,
		@NotNull HttpStatusCode status,
		@NotNull WebRequest request
	) {
		log.info(ex.getMessage());
		final List<ValidationError> errors = ex.getBindingResult().getFieldErrors()
			.stream()
			.map(ValidationError::from)
			.toList();
		return createResponse(CommonErrorCode.INVALID_PARAMETER, errors);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
		return createResponse(CommonErrorCode.INVALID_PARAMETER);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Object> handleConstraintViolationException(
		ConstraintViolationException ex
	) {
		final List<ValidationError> errors = ex.getConstraintViolations().stream()
			.map(violation -> ValidationError.builder()
				.field(violation.getPropertyPath().toString())
				.value(violation.getInvalidValue())
				.message(violation.getMessageTemplate())
				.build())
			.toList();
		return createResponse(CommonErrorCode.INVALID_PARAMETER, errors);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Object> handleMethodArgumentTypeMismatchException(
		MethodArgumentTypeMismatchException ex
	) {
		final ValidationError error = ValidationError.builder()
			.field(ex.getName())
			.value(ex.getValue())
			.message(ex.getMessage())
			.build();
		return createResponse(CommonErrorCode.INVALID_PARAMETER, List.of(error));
	}

	@Override
	protected ResponseEntity<Object> handleMissingPathVariable(
		@NotNull MissingPathVariableException ex,
		@NotNull HttpHeaders headers,
		@NotNull HttpStatusCode status,
		@NotNull WebRequest request
	) {
		return createResponse(CommonErrorCode.MISSING_REQUEST_PARAMETER);
	}

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(
		@NotNull MissingServletRequestParameterException ex,
		@NotNull HttpHeaders headers,
		@NotNull HttpStatusCode status,
		@NotNull WebRequest request
	) {
		return createResponse(CommonErrorCode.MISSING_REQUEST_PARAMETER);
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(
		@NotNull Exception ex,
		Object body,
		@NotNull HttpHeaders headers,
		@NotNull HttpStatusCode statusCode,
		@NotNull WebRequest request
	) {
		final Throwable cause = ex.getCause();
		log.error("{} : {}", cause.getClass(), cause.getMessage());
		log.error("{} : {}", ex.getClass(), ex.getMessage());
		return createResponse(CommonErrorCode.UNEXPECTED_ERROR);
	}

	private ResponseEntity<Object> createResponse(ApiErrorCode errorCode, List<ValidationError> errors) {
		final ApiResponse apiResponse = ApiResponse.builder()
			.code(errorCode.name())
			.message(errorCode.getMessage())
			.errors(errors)
			.build();
		return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
	}

	private ResponseEntity<Object> createResponse(ApiErrorCode errorCode) {
		return createResponse(errorCode, Collections.emptyList());
	}
}
