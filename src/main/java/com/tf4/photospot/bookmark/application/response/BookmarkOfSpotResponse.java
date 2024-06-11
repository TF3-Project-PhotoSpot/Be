package com.tf4.photospot.bookmark.application.response;

import com.querydsl.core.annotations.QueryProjection;

public record BookmarkOfSpotResponse(
	Long bookmarkFolderId,
	String name,
	String color,
	Long bookmarkId
) {
	@QueryProjection
	public BookmarkOfSpotResponse {
	}
}
