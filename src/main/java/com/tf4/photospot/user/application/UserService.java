package com.tf4.photospot.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.global.exception.domain.UserErrorCode;
import com.tf4.photospot.photo.application.PhotoService;
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
	private final PhotoService photoService;

	@Transactional
	public UserProfileResponse updateProfile(Long userId, MultipartFile file, String request) {
		String photoUrl = photoService.uploadOtherPhoto(file, request);
		User loginUser = userRepository.findById(userId)
			.orElseThrow(() -> new ApiException(AuthErrorCode.NOT_FOUND_USER));
		loginUser.updateProfile(photoUrl);
		return new UserProfileResponse(photoUrl);
	}

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
