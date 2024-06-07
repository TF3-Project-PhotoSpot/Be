package com.tf4.photospot.user.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tf4.photospot.global.argument.AuthUserId;
import com.tf4.photospot.global.dto.ApiResponse;
import com.tf4.photospot.photo.application.S3Uploader;
import com.tf4.photospot.photo.domain.S3Directory;
import com.tf4.photospot.user.application.UserService;
import com.tf4.photospot.user.application.response.UserInfoResponse;
import com.tf4.photospot.user.presentation.request.NicknameUpdateRequest;
import com.tf4.photospot.user.presentation.request.UserReportRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserService userService;
	private final S3Uploader s3Uploader;

	@PatchMapping("/me")
	public UserInfoResponse updateUserInfo(
		@AuthUserId Long userId,
		@RequestPart(value = "file", required = false) MultipartFile file,
		@RequestPart(value = "nicknameRequest", required = false) @Valid NicknameUpdateRequest nicknameRequest
	) {
		String imageUrl = file == null ? "" : s3Uploader.upload(file, S3Directory.PROFILE_FOLDER.getFolder());
		String nickname = nicknameRequest == null ? "" : nicknameRequest.nickname();
		return userService.updateUserInfo(userId, imageUrl, nickname);
	}

	@GetMapping("/me")
	public UserInfoResponse getUserInfo(@AuthUserId Long userId) {
		return userService.getInfo(userId);
	}

	@PostMapping("/{offenderId}/reports")
	public ApiResponse reportUser(
		@AuthUserId Long reporterId,
		@PathVariable(name = "offenderId") Long offenderId,
		@RequestBody @Valid UserReportRequest request
	) {
		userService.reportUser(reporterId, offenderId, request.reason());
		return ApiResponse.SUCCESS;
	}
}
