package com.tf4.photospot.auth.application;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tf4.photospot.auth.application.request.AppleRefreshTokenRequest;
import com.tf4.photospot.auth.application.request.AppleRevokeRequest;
import com.tf4.photospot.auth.application.response.ApplePublicKeyDto;
import com.tf4.photospot.auth.application.response.ApplePublicKeyResponse;
import com.tf4.photospot.auth.application.response.AuthUserInfoDto;
import com.tf4.photospot.auth.infrastructure.AppleClient;
import com.tf4.photospot.auth.util.KeyParser;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.global.exception.domain.DetailApiException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppleService {

	private final AppleClient appleClient;

	@Value("${apple.client-id}")
	private String appleBundleId;

	@Value("${apple.key-id}")

	private String appleKeyId;

	@Value("${apple.team-id}")
	private String appleTeamId;

	@Value("${apple.sign-key}")
	private String appleSignKey;

	private static final String KID = "kid";
	private static final String ALG = "alg";
	private static final String APPLE_ID_SERVER = "https://appleid.apple.com";

	public AuthUserInfoDto getTokenInfo(String token, String nonce) {
		Claims claims = getAppleClaims(token);
		validateClaims(claims, nonce);
		return new AuthUserInfoDto(claims.getSubject());
	}

	public Claims getAppleClaims(String identifyToken) {
		Map<String, String> tokenHeaders = parseHeaders(identifyToken);
		PublicKey publicKey = generatePublicKey(tokenHeaders, appleClient.getPublicKey());
		return Jwts.parserBuilder()
			.setSigningKey(publicKey)
			.build()
			.parseClaimsJws(identifyToken)
			.getBody();
	}

	public Map<String, String> parseHeaders(String token) {
		try {
			String header = token.split("\\.")[0];
			return new ObjectMapper().readValue(decodeHeader(header), new TypeReference<>() {
			});
		} catch (JsonProcessingException ex) {
			throw new DetailApiException(AuthErrorCode.FAIL_PARSE_HEADER_FROM_APPLE_TOKEN, ex);
		}
	}

	public String decodeHeader(String token) {
		return new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
	}

	private PublicKey generatePublicKey(Map<String, String> tokenHeaders, ApplePublicKeyResponse applePublicKeys) {
		try {
			ApplePublicKeyDto publicKey = applePublicKeys.getMatchedKey(tokenHeaders.get("kid"),
				tokenHeaders.get("alg"));
			return getPublicKey(publicKey);
		} catch (Exception ex) {
			throw new DetailApiException(AuthErrorCode.FAIL_GENERATE_APPLE_PUBLIC_KEY, ex);
		}
	}

	private PublicKey getPublicKey(ApplePublicKeyDto publicKey) throws
		NoSuchAlgorithmException,
		InvalidKeySpecException {
		byte[] nBytes = Base64.getUrlDecoder().decode(publicKey.n());
		byte[] eBytes = Base64.getUrlDecoder().decode(publicKey.e());

		RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(new BigInteger(1, nBytes), new BigInteger(1, eBytes));
		KeyFactory keyFactory = KeyFactory.getInstance(publicKey.kty());
		return keyFactory.generatePublic(publicKeySpec);
	}

	// TODO : nonce 부분 삭제 여부 결정
	private void validateClaims(Claims claims, String nonce) {
		// validateValue(String.valueOf(claims.get("nonce")), nonce);
		validateValue(claims.getIssuer(), APPLE_ID_SERVER);
		validateValue(claims.getAudience(), appleBundleId);
		validateExpiration(claims.getExpiration());
	}

	private void validateValue(String claimValue, String expectValue) {
		if (!expectValue.equals(claimValue)) {
			throw new ApiException(AuthErrorCode.INVALID_APPLE_IDENTIFY_TOKEN);
		}
	}

	private void validateExpiration(Date expiration) {
		if (expiration.before(new Date())) {
			throw new ApiException(AuthErrorCode.EXPIRED_APPLE_IDENTIFY_TOKEN);
		}
	}

	public void unlink(String authorizationCode) {
		String clientSecret = createClientSecret();
		String refreshToken = getRefreshToken(authorizationCode, clientSecret);
		AppleRevokeRequest request = new AppleRevokeRequest(appleBundleId, clientSecret, refreshToken);
		appleClient.revoke(request.toMultiValueMap());
	}

	private String getRefreshToken(String authorizationCode, String clientSecret) {
		AppleRefreshTokenRequest request = new AppleRefreshTokenRequest(appleBundleId, clientSecret, authorizationCode);
		try {
			return appleClient.generateToken(request.toMultiValueMap()).getRefreshToken();
		} catch (Exception ex) {
			throw new DetailApiException(AuthErrorCode.INVALID_APPLE_AUTHORIZATION_CODE, ex);
		}
	}

	private String createClientSecret() {
		Date expirationDate = Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant());
		return Jwts.builder()
			.setHeaderParam(KID, appleKeyId)
			.setHeaderParam(ALG, "ES256")
			.setIssuer(appleTeamId)
			.setIssuedAt(new Date(System.currentTimeMillis()))
			.setExpiration(expirationDate)
			.setAudience(APPLE_ID_SERVER)
			.setSubject(appleBundleId)
			.signWith(KeyParser.getPrivateKey(appleSignKey), SignatureAlgorithm.ES256)
			.compact();
	}
}
