package com.tf4.photospot.post.application;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.global.dto.SlicePageDto;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.PostErrorCode;
import com.tf4.photospot.global.exception.domain.UserErrorCode;
import com.tf4.photospot.photo.domain.Photo;
import com.tf4.photospot.post.application.request.PostListRequest;
import com.tf4.photospot.post.application.request.PostPreviewListRequest;
import com.tf4.photospot.post.application.request.PostUploadRequest;
import com.tf4.photospot.post.application.response.PostDetailResponse;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.post.application.response.PostWithLikeStatus;
import com.tf4.photospot.post.domain.MentionRepository;
import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.domain.PostRepository;
import com.tf4.photospot.post.domain.PostTag;
import com.tf4.photospot.post.domain.PostTagRepository;
import com.tf4.photospot.post.infrastructure.PostJdbcRepository;
import com.tf4.photospot.post.infrastructure.PostQueryRepository;
import com.tf4.photospot.post.presentation.request.SpotInfoDto;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.spot.domain.SpotRepository;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class PostService {

	private final PostQueryRepository postQueryRepository;
	private final PostJdbcRepository postJdbcRepository;
	private final PostTagRepository postTagRepository;
	private final MentionRepository mentionRepository;
	private final PostRepository postRepository;
	private final SpotRepository spotRepository;
	private final UserRepository userRepository;

	public SlicePageDto<PostDetailResponse> getPosts(PostListRequest request) {
		final Slice<PostWithLikeStatus> postResponses = postQueryRepository.findPostsWithLikeStatus(request);
		final Map<Post, List<PostTag>> postTagGroup = postQueryRepository
			.findPostTagsIn(postResponses.stream().map(PostWithLikeStatus::post).toList())
			.stream()
			.collect(Collectors.groupingBy(PostTag::getPost));
		final List<PostDetailResponse> postDetailResponses = postResponses.stream()
			.map(postResponse -> PostDetailResponse.of(postResponse,
				postTagGroup.getOrDefault(postResponse.post(), Collections.emptyList())))
			.toList();
		return SlicePageDto.wrap(postDetailResponses, postResponses.hasNext());
	}

	public SlicePageDto<PostPreviewResponse> getPostPreviews(PostPreviewListRequest request) {
		return SlicePageDto.wrap(postQueryRepository.findPostPreviews(request));
	}

	@Transactional
	public Long upload(PostUploadRequest request) {
		// Todo : bubble
		Photo photo = Photo.builder()
			.photoUrl(request.getPhotoUrl())
			.coord(request.getPhotoCoord())
			.takenAt(request.getPhotoTakenAt())
			.build();
		User writer = userRepository.findById(request.getUserId())
			.orElseThrow(() -> new ApiException(UserErrorCode.NOT_FOUND_USER));
		Spot spot = findSpotOrCreate(request.getSpotInfoDto());
		Post post = Post.builder()
			.photo(photo)
			.spot(spot)
			.writer(writer)
			.detailAddress(request.getDetailAddress())
			.isPrivate(request.getIsPrivate())
			.build();

		Long postId = postRepository.save(post).getId();
		savePostTags(post, spot.getId(), request.getTags());
		saveMentions(post, request.getMentions());
		return postId;
	}

	private Spot findSpotOrCreate(SpotInfoDto spotInfoDto) {
		return spotRepository.findByCoord(spotInfoDto.coord().toCoord())
			.orElseGet(() -> spotRepository.save(spotInfoDto.toSpot()));
	}

	public void savePostTags(Post post, Long spotId, List<Long> tags) {
		if (tags == null || tags.isEmpty()) {
			return;
		}
		int rowNum = postJdbcRepository.savePostTags(post.getId(), spotId, convertListToString(tags));
		if (rowNum != tags.size()) {
			throw new ApiException(PostErrorCode.NOT_FOUND_TAG);
		}
		post.addTags(postTagRepository.findByPostId(post.getId()));
	}

	public void saveMentions(Post post, List<Long> mentionedUsers) {
		if (mentionedUsers == null || mentionedUsers.isEmpty()) {
			return;
		}
		int rowNum = postJdbcRepository.saveMentions(post.getId(), convertListToString(mentionedUsers));
		if (rowNum != mentionedUsers.size()) {
			throw new ApiException(UserErrorCode.NOT_FOUND_USER);
		}
		post.addMentions(mentionRepository.findByPostId(post.getId()));
	}

	private String convertListToString(List<Long> longValues) {
		return longValues.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(","));
	}
}
