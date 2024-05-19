package com.tf4.photospot.post.presentation;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.global.argument.AuthUserId;
import com.tf4.photospot.global.dto.ApiResponse;
import com.tf4.photospot.global.dto.SlicePageDto;
import com.tf4.photospot.photo.application.PhotoService;
import com.tf4.photospot.photo.domain.S3Directory;
import com.tf4.photospot.post.application.PostService;
import com.tf4.photospot.post.application.request.PostSearchCondition;
import com.tf4.photospot.post.application.request.PostSearchType;
import com.tf4.photospot.post.application.request.PostUpdateRequest;
import com.tf4.photospot.post.application.response.PostDetailResponse;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.post.application.response.PostSaveResponse;
import com.tf4.photospot.post.application.response.PostUpdateResponse;
import com.tf4.photospot.post.presentation.request.PostReportRequest;
import com.tf4.photospot.post.presentation.request.PostStateUpdateRequest;
import com.tf4.photospot.post.presentation.request.PostUpdateHttpRequest;
import com.tf4.photospot.post.presentation.request.PostUploadRequest;
import com.tf4.photospot.post.presentation.request.PostsDeleteHttpRequest;
import com.tf4.photospot.post.presentation.response.TagListResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api/v1/posts")
@RestController
@RequiredArgsConstructor
public class PostController {
	private final PostService postService;
	private final PhotoService photoService;

	@GetMapping
	public SlicePageDto<PostDetailResponse> getPostDetails(
		@RequestParam(name = "spotId") Long spotId,
		@AuthUserId Long userId,
		@SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		final PostSearchCondition searchCondition = PostSearchCondition.builder()
			.spotId(spotId)
			.userId(userId)
			.type(PostSearchType.POSTS_OF_SPOT)
			.pageable(pageable)
			.build();
		return postService.getPosts(searchCondition);
	}

	@GetMapping("/preview")
	public SlicePageDto<PostPreviewResponse> getPostPreviews(
		@RequestParam(name = "spotId") Long spotId,
		// @AuthUserId Long userId,
		@SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		Long userId = 1L;
		final PostSearchCondition searchCondition = PostSearchCondition.builder()
			.spotId(spotId)
			.userId(userId)
			.type(PostSearchType.POSTS_OF_SPOT)
			.pageable(pageable)
			.build();
		return postService.getPostPreviews(searchCondition);
	}

	@PostMapping("{postId}/likes")
	public ApiResponse likePost(
		@PathVariable(name = "postId") Long postId,
		@AuthUserId Long userId
	) {
		postService.likePost(postId, userId);
		return ApiResponse.SUCCESS;
	}

	@DeleteMapping("{postId}/likes")
	public ApiResponse cancelPostLike(
		@PathVariable(name = "postId") Long postId,
		@AuthUserId Long userId
	) {
		postService.cancelPostLike(postId, userId);
		return ApiResponse.SUCCESS;
	}

	@GetMapping("/mine/preview")
	public SlicePageDto<PostPreviewResponse> getMyPosts(
		@AuthUserId Long userId,
		@SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		final PostSearchCondition searchCondition = PostSearchCondition.builder()
			.userId(userId)
			.type(PostSearchType.MY_POSTS)
			.pageable(pageable)
			.build();
		return postService.getPostPreviews(searchCondition);
	}

	@GetMapping("/mine")
	public SlicePageDto<PostDetailResponse> getMyPostDetails(
		@AuthUserId Long userId,
		@SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		final PostSearchCondition searchCondition = PostSearchCondition.builder()
			.userId(userId)
			.type(PostSearchType.MY_POSTS)
			.pageable(pageable)
			.build();
		return postService.getPosts(searchCondition);
	}

	@GetMapping("/likes/preview")
	public SlicePageDto<PostPreviewResponse> getLikePostPreviews(
		@AuthUserId Long userId,
		@SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		final PostSearchCondition searchCondition = PostSearchCondition.builder()
			.userId(userId)
			.type(PostSearchType.LIKE_POSTS)
			.pageable(pageable)
			.build();
		return postService.getPostPreviews(searchCondition);
	}

	@GetMapping("/likes")
	public SlicePageDto<PostDetailResponse> getLikePostDetails(
		@AuthUserId Long userId,
		@SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		final PostSearchCondition searchCondition = PostSearchCondition.builder()
			.userId(userId)
			.type(PostSearchType.LIKE_POSTS)
			.pageable(pageable)
			.build();
		return postService.getPosts(searchCondition);
	}

	@PostMapping
	public PostSaveResponse uploadPost(
		@AuthUserId Long userId,
		@RequestBody @Valid PostUploadRequest request) {
		String postPhotoUrl = photoService.moveFolder(request.photoInfo().photoUrl(), S3Directory.TEMP_FOLDER,
			S3Directory.POST_FOLDER);
		return postService.upload(userId, request, postPhotoUrl);
	}

	@PutMapping("/{postId}")
	public PostUpdateResponse updatePost(
		@AuthUserId Long userId,
		@PathVariable("postId") Long postId,
		@RequestBody @Valid PostUpdateHttpRequest request) {
		return postService.update(PostUpdateRequest.of(userId, postId, request));
	}

	@PatchMapping("/{postId}")
	public PostUpdateResponse updatePrivacyState(
		@AuthUserId Long userId,
		@PathVariable("postId") Long postId,
		@RequestBody @Valid PostStateUpdateRequest request) {
		return postService.updatePrivacyState(userId, postId, request.isPrivate());
	}

	@DeleteMapping("/{postId}")
	public ApiResponse deletePost(
		@AuthUserId Long userId,
		@PathVariable("postId") Long postId) {
		postService.delete(userId, postId);
		return ApiResponse.SUCCESS;
	}

	@PostMapping("/{postId}/report")
	public ApiResponse reportPost(
		@AuthUserId Long userId,
		@PathVariable("postId") Long postId,
		@RequestBody @Valid PostReportRequest request) {
		postService.report(userId, postId, request.reason());
		return ApiResponse.SUCCESS;
	}

	@GetMapping("/tags")
	public TagListResponse getTags() {
		return TagListResponse.from(postService.getTags());
	}

	@DeleteMapping
	public ApiResponse deletePosts(
		@AuthUserId Long userId,
		@RequestBody @Valid PostsDeleteHttpRequest request
	) {
		postService.deletePostsBy(userId, request.postIds());
		return ApiResponse.SUCCESS;
	}
}
