package com.tf4.photospot.map.application.response.kakao;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.tf4.photospot.global.dto.CoordinateDto;

public class KakaoSearchLocationDeserializer extends JsonDeserializer<KakaoSearchLocationResponse> {
	private static final String ADDRESS_KEY = "address_name";
	private static final String LAT_KEY = "y";
	private static final String LON_KEY = "x";

	@Override
	public KakaoSearchLocationResponse deserialize(JsonParser parser, DeserializationContext ctxt) throws
		IOException {
		final JsonNode jsonNode = parser.getCodec().readTree(parser);
		final JsonNode address = jsonNode.findValue(ADDRESS_KEY);
		final JsonNode lon = jsonNode.findValue(LON_KEY);
		final JsonNode lat = jsonNode.findValue(LAT_KEY);

		if (address == null || lat == null || lon == null) {
			return KakaoSearchLocationResponse.ERROR_RESPONSE;
		}

		return KakaoSearchLocationResponse.builder()
			.address(address.asText())
			.coord(new CoordinateDto(lon.asDouble(), lat.asDouble()))
			.build();
	}
}
