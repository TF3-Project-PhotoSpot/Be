package com.tf4.photospot.auth.application.response;

public record ApplePublicKeyDto(
	String kty,
	String kid,
	String alg,
	String n,
	String e
) {
}
