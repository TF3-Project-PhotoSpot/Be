package com.tf4.photospot.auth.application.response;

public record ApplePublicKey(
	String kty,
	String kid,
	String alg,
	String n,
	String e
) {
}
