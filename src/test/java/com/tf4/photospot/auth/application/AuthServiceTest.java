package com.tf4.photospot.auth.application;

import static com.tf4.photospot.support.TestFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;
import static org.mockito.BDDMockito.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.auth.application.response.ReissueTokenResponse;
import com.tf4.photospot.auth.domain.RefreshToken;
import com.tf4.photospot.auth.infrastructure.JwtRedisRepository;
import com.tf4.photospot.auth.infrastructure.KakaoClient;
import com.tf4.photospot.auth.presentation.request.KakaoUnlinkCallbackInfo;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.global.exception.domain.UserErrorCode;
import com.tf4.photospot.global.util.SlackAlert;
import com.tf4.photospot.mockobject.MockKakaoAuthConfig;
import com.tf4.photospot.mockobject.WithCustomMockUser;
import com.tf4.photospot.support.IntegrationTestSupport;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;
import com.tf4.photospot.user.infrastructure.UserQueryRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Import(MockKakaoAuthConfig.class)
public class AuthServiceTest extends IntegrationTestSupport {
	private final JwtService jwtService;
	private final UserRepository userRepository;
	private final JwtRedisRepository jwtRedisRepository;

	@SpyBean
	private final AuthService authService;

	@SpyBean
	private UserQueryRepository userQueryRepository;

	@SpyBean
	private KakaoService kakaoService;

	@MockBean
	private SlackAlert slackAlert;

	@SpyBean
	private KakaoClient kakaoClient;

	@MockBean
	private AppleService appleService;

	@TestFactory
	@DisplayName("사용자 등록 시나리오")
	Collection<DynamicTest> saveUser() {
		// given
		String providerType = "kakao";
		String account = "456789";

		return List.of(
			dynamicTest("카카오 로그인에 성공한다.", () -> {
				var accessToken = "access_token";
				var response = authService.kakaoLogin(accessToken, account);
				assertAll(
					() -> assertFalse(response.hasLoggedInBefore()),
					() -> assertThat(
						userRepository.findById(response.getId()).orElseThrow().getProviderType()).isEqualTo("kakao")
				);
			}),
			dynamicTest("기존에 로그인 했던 사용자는 DB에 저장하지 않는다.", () -> {
				//when
				var loginResponse2 = authService.oauthLogin(providerType, account);
				User savedUser = userRepository.findUserByProviderTypeAndAccount(providerType, account).orElseThrow();

				//then
				assertAll(
					() -> assertThat(loginResponse2.hasLoggedInBefore()).isTrue(),
					() -> assertThat(savedUser.getAccount()).isEqualTo(account)
				);
			})
		);
	}

	@Test
	@WithCustomMockUser
	@DisplayName("DB에 저장된 리프레시 토큰과 전달 받은 토큰이 같으면 액세스 토큰을 재발급 한다.")
	void reissue() {
		// given
		Long userId = userRepository.save(createUser("사용자", "1234", "kakao")).getId();
		String refreshToken = jwtService.issueRefreshToken(userId);

		// when
		ReissueTokenResponse tokenResponse = authService.reissueToken(userId, refreshToken);

		// then
		assertThat(tokenResponse.accessToken()).isNotBlank();
	}

	@TestFactory
	@WithCustomMockUser
	Stream<DynamicTest> logout() {
		// given
		User loginUser = createUser("사용자", "1234", "kakao");
		userRepository.save(loginUser);
		jwtRedisRepository.save(new RefreshToken(loginUser.getId(), "refresh_token"));
		String accessToken = "Bearer " + jwtService.issueAccessToken(loginUser.getId(), loginUser.getRole().getType());

		return Stream.of(
			dynamicTest("로그아웃을 하면 Redis에 저장된 리프레시 토큰을 삭제하고 블랙리스트에 액세스 토큰을 추가한다.", () -> {
				// when
				authService.logout(accessToken);

				// then
				assertAll(
					() -> assertThat(jwtRedisRepository.findByUserId(loginUser.getId())).isEqualTo(Optional.empty()),
					() -> assertThatThrownBy(() -> authService.existsBlacklist(accessToken))
						.isInstanceOf(ApiException.class).hasMessage(AuthErrorCode.INVALID_ACCESS_TOKEN.getMessage())
				);
			}),
			dynamicTest("유효하지 않은 액세스 토큰으로 로그아웃 요청 시 예외를 던진다.", () -> {
				// given
				String wrongAccessToken = "wrong_access_token";

				// when & then
				assertThatThrownBy(() -> authService.logout(wrongAccessToken))
					.isInstanceOf(ApiException.class).hasMessage(AuthErrorCode.UNAUTHORIZED_USER.getMessage());
			})
		);
	}

	@TestFactory
	@WithCustomMockUser
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	Stream<DynamicTest> withdrawal() {
		// given
		User loginUser = createUser("사용자", "0505", "kakao");
		userRepository.save(loginUser);
		jwtRedisRepository.save(new RefreshToken(loginUser.getId(), "refresh_token"));
		String accessToken = "Bearer " + jwtService.issueAccessToken(loginUser.getId(), loginUser.getRole().getType());

		return Stream.of(
			dynamicTest("카카오 회원탈퇴 시 카카오 서버와 통신하여 계정을 끊는다.", () -> {
				var userId = loginUser.getId();
				authService.unlinkAccountFromOauthServer(userId, null);
				verify(authService, times(1)).unlinkKakaoAccount(any(User.class));
				verify(kakaoService, times(1)).unlink(anyLong());
				verify(kakaoClient, times(1)).unlink(anyString(), anyString(), anyString());
			}),
			dynamicTest("회원탈퇴 시 user 정보를 업데이트하고 JWT 토큰을 무효회한다.", () -> {
				// given
				assertNull(loginUser.getDeletedAt());

				// when
				authService.deleteUser(loginUser.getId(), accessToken);
				User deletedUser = userRepository.findById(loginUser.getId()).orElseThrow();

				// then
				assertAll(
					() -> assertNotNull(deletedUser.getDeletedAt()),
					() -> assertThat(deletedUser.getAccount()).startsWith(deletedUser.getId() + "_deleted_"),
					() -> assertThat(jwtRedisRepository.findByUserId(deletedUser.getId())).isEqualTo(Optional.empty()),
					() -> assertThat(userQueryRepository.findActiveUserById(deletedUser.getId())).isEqualTo(
						Optional.empty()),
					() -> assertThatThrownBy(() -> authService.existsBlacklist(accessToken))
						.isInstanceOf(ApiException.class).hasMessage(AuthErrorCode.INVALID_ACCESS_TOKEN.getMessage())
				);
			}),
			dynamicTest("존재하지 않거나 이미 탈퇴한 회원에 대해 회원탈퇴 시 예외를 던진다.", () -> {
				// when & then
				assertThatThrownBy(() -> authService.deleteUser(loginUser.getId(), accessToken))
					.isInstanceOf(ApiException.class).hasMessage(UserErrorCode.NOT_FOUND_USER.getMessage());
			}),
			dynamicTest("카카오 콜백 요청에 따라 연결이 끊긴 사용자를 탈퇴처리한다.", () -> {
				// given
				User user = createUser("사용자", "100", "kakao");
				userRepository.save(user);
				jwtRedisRepository.save(new RefreshToken(user.getId(), "refresh_token"));
				String adminKey = "admin-key-admin-key-admin-key";
				var info = new KakaoUnlinkCallbackInfo("123456", "100", "test");

				// when
				authService.deleteUnlinkedKakaoUser(adminKey, info);
				User deletedUser = userRepository.findById(user.getId()).orElseThrow();

				// then
				assertAll(
					() -> assertNotNull(deletedUser.getDeletedAt()),
					() -> assertThat(deletedUser.getAccount()).startsWith(deletedUser.getId() + "_deleted_"),
					() -> assertThat(jwtRedisRepository.findByUserId(deletedUser.getId())).isEqualTo(Optional.empty())
				);
				verify(slackAlert, times(0)).sendKakaoCallbackFailure(any(Exception.class), anyString(), anyString());
			}),
			dynamicTest("카카오 콜백 요청에 유효하지 않는 adminKey이 포함되어 있을 시 예외를 던진다", () -> {
				// given
				String adminKey = "wrong-admin-key";
				var info = new KakaoUnlinkCallbackInfo("123456", "0505", "test");

				// when & then
				assertThatThrownBy(() -> authService.deleteUnlinkedKakaoUser(adminKey, info)).isInstanceOf(
					ApiException.class).hasMessage(AuthErrorCode.INVALID_KAKAO_REQUEST.getMessage());
			}),
			dynamicTest("카카오 콜백 요청에 유효하지 않는 app id가 포함되어 있을 시 예외를 던진다", () -> {
				// given
				String adminKey = "admin-key-admin-key-admin-key";
				String wrongAppId = "0000";
				var info = new KakaoUnlinkCallbackInfo(wrongAppId, "0505", "test");

				// when & then
				assertThatThrownBy(() -> authService.deleteUnlinkedKakaoUser(adminKey, info)).isInstanceOf(
					ApiException.class).hasMessage(AuthErrorCode.INVALID_KAKAO_REQUEST.getMessage());
			}),
			dynamicTest("카카오 콜백 요청 처리 중 해당 사용자가 존재하지 않거나 이미 탈퇴한 회원이면 슬랙 알림 메서드를 호출한다.", () -> {
				// given
				String adminKey = "admin-key-admin-key-admin-key";
				var info = new KakaoUnlinkCallbackInfo("123456", "0505", "test");

				// when
				authService.deleteUnlinkedKakaoUser(adminKey, info);

				// then
				verify(slackAlert).sendKakaoCallbackFailure(any(Exception.class), anyString(), anyString());
			}),
			dynamicTest("카카오 콜백 요청 처리 중 DB 업데이트가 실패하면 슬랙 알림 메서드를 호출한다.", () -> {
				// given
				User user = createUser("사용자", "5050", "kakao");
				userRepository.save(user);
				jwtRedisRepository.save(new RefreshToken(user.getId(), "refresh_token"));
				String adminKey = "admin-key-admin-key-admin-key";
				var info = new KakaoUnlinkCallbackInfo("123456", "50500", "test");
				doThrow(new RuntimeException("Test exception")).when(userQueryRepository).deleteByUserId(anyLong());

				// when
				authService.deleteUnlinkedKakaoUser(adminKey, info);

				// then
				verify(slackAlert, times(2)).sendKakaoCallbackFailure(any(Exception.class), anyString(), anyString());
			}),
			dynamicTest("애플 계정 끊기 중 인증 코드가 비어있으면 예외를 던진다.", () -> {
				var authorizationCode = "";
				assertThatThrownBy(() -> authService.unlinkAppleAccount(authorizationCode))
					.isInstanceOf(ApiException.class)
					.hasMessage(AuthErrorCode.EMPTY_APPLE_AUTHORIZATION_CODE.getMessage());
			}),
			dynamicTest("애플 계정 끊기 호출을 성공한다.", () -> {
				var authorizationCode = "authorization_code";
				authService.unlinkAppleAccount(authorizationCode);
				verify(appleService, times(1)).unlink(anyString());
			})
		);
	}
}
