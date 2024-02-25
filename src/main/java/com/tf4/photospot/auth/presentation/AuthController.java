package com.tf4.photospot.auth.presentation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.auth.application.AuthService;
import com.tf4.photospot.auth.application.response.ReissueTokenResponse;
import com.tf4.photospot.global.argument.AuthUserId;
import com.tf4.photospot.global.config.jwt.JwtConstant;
import com.tf4.photospot.global.dto.LoginUserDto;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {

	private final AuthService authService;

	@GetMapping("/reissue")
	public ReissueTokenResponse reissueToken(
		@AuthenticationPrincipal LoginUserDto loginUserDto,
		@RequestHeader(JwtConstant.AUTHORIZATION_HEADER) String refreshToken) {
		return authService.reissueToken(loginUserDto.getId(), refreshToken);
	}

	@DeleteMapping
	public void logout(
		@AuthUserId Long userId,
		@RequestHeader(JwtConstant.AUTHORIZATION_HEADER) String accessToken) {
		authService.logout(userId, accessToken);
	}
}
