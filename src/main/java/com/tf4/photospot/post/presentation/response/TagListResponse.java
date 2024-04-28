package com.tf4.photospot.post.presentation.response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.tf4.photospot.post.application.response.TagResponse;

public record TagListResponse(
	Map<String, List<TagResponse>> tags
) {
	public static TagListResponse from(List<TagResponse> tagResponses) {
		return new TagListResponse(tagResponses.stream()
			.collect(Collectors.groupingBy(tagResponse -> tagResponse.tagType())));
	}
}
