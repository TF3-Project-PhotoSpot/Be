package com.tf4.photospot.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.UserErrorCode;
import com.tf4.photospot.user.application.response.UserInfoResponse;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;
import com.tf4.photospot.user.infrastructure.UserQueryRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserQueryRepository userQueryRepository;
	private final UserRepository userRepository;

	@Transactional
	public UserInfoResponse updateUserInfo(Long userId, String imageUrl, String nickname) {
		User user = getActiveUser(userId);
		updateNickname(user, nickname);
		updateProfile(user, imageUrl);
		return UserInfoResponse.builder()
			.userId(userId)
			.nickname(user.getNickname())
			.profileUrl(user.getProfileUrl())
			.provider(user.getProviderType())
			.build();
	}

	private void updateNickname(User user, String nickname) {
		if (!StringUtils.hasText(nickname)) {
			return;
		}
		if (isNicknameDuplicated(nickname)) {
			throw new ApiException(UserErrorCode.DUPLICATE_NICKNAME);
		}
		user.updateNickname(nickname);
	}

	private void updateProfile(User user, String imageUrl) {
		if (StringUtils.hasText(imageUrl)) {
			user.updateProfile(imageUrl);
		}
	}

	public UserInfoResponse getInfo(Long userId) {
		return UserInfoResponse.of(getActiveUser(userId));
	}

	public User getActiveUser(Long userId) {
		return userQueryRepository.findActiveUserById(userId)
			.orElseThrow(() -> new ApiException(UserErrorCode.NOT_FOUND_USER));
	}

	public boolean isNicknameDuplicated(String nickname) {
		return userRepository.existsByNickname(nickname);
	}
}
