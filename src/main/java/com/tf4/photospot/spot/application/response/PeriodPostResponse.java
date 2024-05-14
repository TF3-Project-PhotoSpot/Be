package com.tf4.photospot.spot.application.response;

import java.time.LocalDateTime;

import org.locationtech.jts.geom.Point;

import com.querydsl.core.annotations.QueryProjection;

public record PeriodPostResponse(
	Long postId,
	Long spotId,
	Point coord,
	String photoUrl,
	LocalDateTime takenAt
) {
	@QueryProjection
	public PeriodPostResponse {
	}
}
