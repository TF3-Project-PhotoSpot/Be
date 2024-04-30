package com.tf4.photospot.map.application.response.kakao;

import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class KakaoCoordToAddressDeserializer extends JsonDeserializer<KakaoCoordToAddressResponse> {
	private static final String ADDRESS_KEY = "address_name";

	@Override
	public KakaoCoordToAddressResponse deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		final JsonNode jsonNode = parser.getCodec().readTree(parser);
		return Optional.ofNullable(jsonNode.findValue(ADDRESS_KEY))
			.map(address -> new KakaoCoordToAddressResponse(address.asText()))
			.orElseGet(() -> KakaoCoordToAddressResponse.ERROR_RESPONSE);
	}
}
