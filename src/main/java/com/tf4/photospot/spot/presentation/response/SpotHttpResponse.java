package com.tf4.photospot.spot.presentation.response;

import java.util.List;

import com.tf4.photospot.bookmark.application.response.BookmarkOfSpotResponse;
import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.global.util.PointConverter;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.spot.application.response.MostPostTagRank;
import com.tf4.photospot.spot.application.response.SpotResponse;

import lombok.Builder;

public record SpotHttpResponse(
	Integer distance,
	Long id,
	String address,
	CoordinateDto coord,
	Long postCount,
	List<MostPostTagRank> tags,
	List<String> photoUrls,
	List<BookmarkOfSpotResponse> bookmarks
) {
	@Builder
	public SpotHttpResponse {
		if (distance == null) {
			distance = 0;
		}
	}

	public static SpotHttpResponse of(
		Integer distance,
		SpotResponse response,
		List<BookmarkOfSpotResponse> bookmarks
	) {
		return SpotHttpResponse.builder()
			.distance(distance)
			.id(response.id())
			.address(response.address())
			.coord(PointConverter.convert(response.coord()))
			.postCount(response.postCount())
			.tags(response.mostPostTagRanks())
			.photoUrls(response.previewResponses().stream().map(PostPreviewResponse::photoUrl).toList())
			.bookmarks(bookmarks)
			.build();
	}
}
