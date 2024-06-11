package com.tf4.photospot.post.application.response;

public record RecentPostPreviewResponse(
	Long spotId,
	Long postId,
	String photoUrl
) {
}
