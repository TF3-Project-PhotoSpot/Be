package com.tf4.photospot.post.application.response;

import com.tf4.photospot.post.domain.PostTag;
import com.tf4.photospot.post.domain.Tag;

import io.micrometer.common.util.StringUtils;
import lombok.Builder;

public record TagResponse(
	Long tagId,
	String iconUrl,
	String tagType,
	String tagName
) {
	@Builder
	public TagResponse {
		if (StringUtils.isEmpty(iconUrl)) {
			iconUrl = "";
		}
	}

	public static TagResponse from(PostTag postTag) {
		final Tag tag = postTag.getTag();
		return TagResponse.builder()
			.tagId(tag.getId())
			.tagType(tag.getTagType().name())
			.iconUrl(tag.getIconUrl())
			.tagName(tag.getName())
			.build();
	}
}
