package com.tf4.photospot.global.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ReqResLoggingFilter extends OncePerRequestFilter {

	private static final String REQUEST_ID = "request_id";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
		ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

		long startTime = System.currentTimeMillis();
		filterChain.doFilter(requestWrapper, responseWrapper);
		long endTime = System.currentTimeMillis();

		try {
			String requestId = UUID.randomUUID().toString().substring(0, 8);
			MDC.put(REQUEST_ID, requestId);
			log.info("""
					[REQUEST] {} - {} {} - {}
					HEADERS : {}
					REQUEST PARAMS : {}
					REQUEST BODY : {}
					RESPONSE BODY : {}
					""", request.getMethod(), request.getRequestURI(), responseWrapper.getStatus(),
				(endTime - startTime) / 1000.0, getHeaders(request), getRequestParams(request),
				getRequestBody(requestWrapper), getResponseBody(responseWrapper));
			responseWrapper.copyBodyToResponse();
		} catch (Exception ex) {
			log.error("[" + this.getClass().getSimpleName() + "] Logging 실패", ex);
		} finally {
			MDC.remove(REQUEST_ID);
		}
	}

	private Map<String, String> getHeaders(HttpServletRequest request) {
		return Collections.list(request.getHeaderNames())
			.stream()
			.collect(Collectors.toMap(headerName -> headerName, request::getHeader));
	}

	private Map<String, String> getRequestParams(HttpServletRequest request) {
		Map<String, String[]> params = request.getParameterMap();
		return params.entrySet()
			.stream()
			.collect(Collectors.toMap(Map.Entry::getKey, entry -> String.join(", ", entry.getValue())));
	}

	private String getRequestBody(ContentCachingRequestWrapper requestWrapper) {
		ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(requestWrapper,
			ContentCachingRequestWrapper.class);
		if (wrapper != null && wrapper.getContentLength() > 0) {
			return new String(wrapper.getContentAsByteArray());
		}
		return "";
	}

	private String getResponseBody(final HttpServletResponse response) {
		ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response,
			ContentCachingResponseWrapper.class);
		if (wrapper != null && wrapper.getContentSize() > 0) {
			return new String(wrapper.getContentAsByteArray());
		}
		return "";
	}
}
