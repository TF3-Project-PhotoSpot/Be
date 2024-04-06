package com.tf4.photospot.spot.application.response;

import java.time.LocalDateTime;

public record IncludedPostResponse(
	Long postId,
	String photoUrl,
	LocalDateTime takenAt
) {
	public static IncludedPostResponse from(PeriodPostResponse response) {
		return new IncludedPostResponse(response.postId(), response.photoUrl(), response.takenAt());
	}
}
