package com.tf4.photospot.bookmark.application.response;

import java.util.Collections;
import java.util.List;

import com.tf4.photospot.bookmark.domain.Bookmark;
import com.tf4.photospot.post.application.response.RecentPostPreviewResponse;

import lombok.Builder;
import software.amazon.awssdk.utils.CollectionUtils;

public record BookmarkResponse(
	Long id,
	Long spotId,
	String name,
	String address,
	List<String> photoUrls
) {
	@Builder
	public BookmarkResponse {
	}

	public static BookmarkResponse of(Bookmark bookmark, List<RecentPostPreviewResponse> postPreviewResponses) {
		return BookmarkResponse.builder()
			.id(bookmark.getId())
			.spotId(bookmark.getSpotId())
			.name(bookmark.getName())
			.address(bookmark.getSpotAddress())
			.photoUrls(getPhotoUrls(postPreviewResponses))
			.build();
	}

	private static List<String> getPhotoUrls(List<RecentPostPreviewResponse> postPreviews) {
		if (CollectionUtils.isNullOrEmpty(postPreviews)) {
			return Collections.emptyList();
		}
		return postPreviews.stream().map(RecentPostPreviewResponse::photoUrl).toList();
	}
}
