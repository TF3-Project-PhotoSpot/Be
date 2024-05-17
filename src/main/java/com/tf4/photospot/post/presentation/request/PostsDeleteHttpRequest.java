package com.tf4.photospot.post.presentation.request;

import java.util.List;

import jakarta.validation.constraints.Positive;

public record PostsDeleteHttpRequest(
	List<@Positive Long> postIds
) {
}
