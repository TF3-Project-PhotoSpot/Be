package com.tf4.photospot.user.application;

import static com.tf4.photospot.support.TestFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.UserErrorCode;
import com.tf4.photospot.support.IntegrationTestSupport;
import com.tf4.photospot.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserServiceTest extends IntegrationTestSupport {
	private final UserService userService;
	private final UserRepository userRepository;

	@TestFactory
	Stream<DynamicTest> updateUserInfo() {
		var user = createUser("nickname", "123456", "kakao");
		user.updateProfile("https://bucket.s3.ap-northeast-2.amazonaws.com/profile/example.webp");
		var userId = userRepository.save(user).getId();
		createUser("중복닉네임", "456789", "apple");

		return Stream.of(
			dynamicTest("닉네임만 변경하는 경우 프로필 사진은 그대로 유지한다.", () -> {
				String newNickname = "새로운_Nickname1";
				String emptyImageUrl = "";
				userService.updateUserInfo(userId, emptyImageUrl, newNickname);
				assertThat(user.getNickname()).isEqualTo(newNickname);
				assertThat(user.getProfileUrl()).isEqualTo(
					"https://bucket.s3.ap-northeast-2.amazonaws.com/profile/example.webp");
			}),
			dynamicTest("프로필 사진만 변경하는 경우 닉네임은 그대로 유지한다.", () -> {
				String newImageUrl = "https://bucket.s3.ap-northeast-2.amazonaws.com/profile/newExample.webp";
				String emptyNickname = "";
				userService.updateUserInfo(userId, newImageUrl, emptyNickname);
				assertThat(user.getNickname()).isEqualTo("새로운_Nickname1");
				assertThat(user.getProfileUrl()).isEqualTo(newImageUrl);
			}),
			dynamicTest("닉네임과 프로필 사진 모두 변경 성공한다.", () -> {
				String newNickname = "또다른.Nickname1";
				String newImageUrl = "https://bucket.s3.ap-northeast-2.amazonaws.com/profile/otherExample.webp";
				userService.updateUserInfo(userId, newImageUrl, newNickname);
				assertThat(user.getNickname()).isEqualTo(newNickname);
				assertThat(user.getProfileUrl()).isEqualTo(newImageUrl);
			}),
			dynamicTest("중복되는 닉네임으로 변경하는 경우 예외를 던진다.", () -> {
				String duplicatedNickname = "중복닉네임";
				String newImageUrl = "https://bucket.s3.ap-northeast-2.amazonaws.com/profile/newExample.webp";
				userService.updateUserInfo(userId, newImageUrl, duplicatedNickname);
				assertThatThrownBy(() -> userService.updateUserInfo(userId, newImageUrl, duplicatedNickname))
					.isInstanceOf(ApiException.class).hasMessage(UserErrorCode.DUPLICATE_NICKNAME.getMessage());
			})
		);
	}

	@TestFactory
	Stream<DynamicTest> getUserInfo() {
		var user = createUser("사용자", "123456", "kakao");
		user.updateProfile("image.com");
		Long userId = userRepository.save(user).getId();
		return Stream.of(
			dynamicTest("사용자 정보 조회를 성공한다.", () -> {
				var response = userService.getInfo(userId);
				assertThat(response.userId()).isEqualTo(userId);
				assertThat(response.nickname()).isEqualTo("사용자");
				assertThat(response.profileUrl()).isEqualTo("image.com");
				assertThat(response.provider()).isEqualTo("kakao");
			}),
			dynamicTest("존재하지 않는 사용자 정보 조회 시 예외를 던진다.", () -> {
				assertThatThrownBy(() -> userService.getInfo(100L))
					.isInstanceOf(ApiException.class).hasMessage(UserErrorCode.NOT_FOUND_USER.getMessage());
			})
		);
	}
}
