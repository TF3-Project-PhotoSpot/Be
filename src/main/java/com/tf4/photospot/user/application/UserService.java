package com.tf4.photospot.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.global.exception.domain.UserErrorCode;
import com.tf4.photospot.user.application.request.LoginUserInfo;
import com.tf4.photospot.user.application.response.OauthLoginUserResponse;
import com.tf4.photospot.user.application.response.UserProfileResponse;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;
import com.tf4.photospot.user.util.NicknameGenerator;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	@Transactional
	public UserProfileResponse updateProfile(Long userId, String imageUrl) {
		// 필요한 부분?
		User loginUser = userRepository.findById(userId)
			.orElseThrow(() -> new ApiException(AuthErrorCode.NOT_FOUND_USER));
		loginUser.updateProfile(imageUrl);
		return new UserProfileResponse(imageUrl);
	}

	// Todo : 로그인 관련 메서드 AuthService 옮기기
	@Transactional
	public OauthLoginUserResponse oauthLogin(String providerType, String account) {
		return userRepository.findUserByProviderTypeAndAccount(providerType, account)
			.map(findUser -> OauthLoginUserResponse.from(true, findUser))
			.orElseGet(() -> OauthLoginUserResponse.from(false, userRepository.save(
				new LoginUserInfo(providerType, account).toUser(generateNickname())
			)));
	}

	public User findUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new ApiException(UserErrorCode.NOT_FOUND_USER));
	}

	private boolean isNicknameDuplicated(String nickname) {
		return userRepository.existsByNickname(nickname);
	}

	private String generateNickname() {
		String generatedRandomNickname = NicknameGenerator.generatorRandomNickname();
		while (isNicknameDuplicated(generatedRandomNickname)) {
			generatedRandomNickname = NicknameGenerator.generatorRandomNickname();
		}
		return generatedRandomNickname;
	}
}
