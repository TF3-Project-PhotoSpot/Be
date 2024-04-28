package com.tf4.photospot.post.presentation.response;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tf4.photospot.post.application.response.TagResponse;
import com.tf4.photospot.post.domain.TagType;

class TagListResponseTest {
	@Test
	void test() throws JsonProcessingException {
		//given
		final TagListResponse tagListResponse = TagListResponse.from(List.of(
			TagResponse.builder()
				.tagId(1L)
				.tagName("tag1")
				.tagType(TagType.ETC.name()).build(),
			TagResponse.builder()
				.tagId(1L)
				.tagName("tag1")
				.tagType(TagType.POSITIVE.name()).build()
		));

		//when
		final String s = new ObjectMapper().writeValueAsString(tagListResponse);

		//then
		System.out.println(s);

	}
}