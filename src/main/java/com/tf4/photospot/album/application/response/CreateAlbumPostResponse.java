package com.tf4.photospot.album.application.response;

import lombok.Builder;

public record CreateAlbumPostResponse(
	Long postId,
	boolean isDuplicated
) {
	@Builder
	public CreateAlbumPostResponse {
	}
}
