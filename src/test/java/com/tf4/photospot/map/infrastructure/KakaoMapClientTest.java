package com.tf4.photospot.map.infrastructure;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.tf4.photospot.global.config.maps.KakaoMapProperties;
import com.tf4.photospot.global.config.maps.MapApiConfig;
import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.map.application.response.kakao.KakaoCoordToAddressResponse;
import com.tf4.photospot.map.application.response.kakao.KakaoSearchLocationResponse;
import com.tf4.photospot.support.RestClientTestSupport;

class KakaoMapClientTest extends RestClientTestSupport {
	private final MockRestServiceServer mockServer;
	private final KakaoMapClient kakaoMapClient;

	public KakaoMapClientTest(
		RestClient.Builder restClientBuilder,
		KakaoMapProperties properties
	) {
		mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
		kakaoMapClient = new MapApiConfig(properties).kakaoMapClient(restClientBuilder);
	}

	@DisplayName("주소로 지역을 검색한다.")
	@Test
	void searchAddress() {
		//given
		CoordinateDto expectedCoord = new CoordinateDto(126.99597295767953d, 35.97664845766847d);
		String expectedUri = UriComponentsBuilder.fromHttpUrl("https://dapi.kakao.com/v2/local/search/address.json")
			.queryParam("query", "전북 삼성동 100")
			.encode(StandardCharsets.UTF_8)
			.build().toUriString();
		final String expectedResponse = """
			{
				"meta": {
					"total_count": 4,
					"pageable_count": 4,
					"is_end": true
				},
				"documents": [
					{
						"address_name": "전북 익산시 부송동 100",
						"y": "35.97664845766847",
						"x": "126.99597295767953",
						"address_type": "REGION_ADDR",
						"address": {
							"address_name": "전북 익산시 부송동 100",
							"region_1depth_name": "전북",
							"region_2depth_name": "익산시",
							"region_3depth_name": "부송동",
							"region_3depth_h_name": "삼성동",
							"x": "126.99597295767953",
							"y": "35.97664845766847"
						},
						"road_address": {
							"address_name": "전북 익산시 망산길 11-17",
							"region_1depth_name": "전북",
							"region_2depth_name": "익산시",
							"region_3depth_name": "부송동",
							"y": "35.976749396987046",
							"x": "126.99599512792346"
						}
					}
				]
			}
			""";
		mockServer.expect(requestTo(expectedUri))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON));
		//when
		final KakaoSearchLocationResponse response = kakaoMapClient.searchAddress("전북 삼성동 100");
		//then
		assertThat(response).isNotNull();
		assertThat(response.address()).isEqualTo("전북 익산시 부송동 100");
		assertThat(response.coord()).isEqualTo(expectedCoord);
	}

	@DisplayName("좌표를 지도상 주소로 변환한다.")
	@Test
	void convertCoordToAddress() {
		//given
		String expectedUri = UriComponentsBuilder.fromHttpUrl("https://dapi.kakao.com/v2/local/geo/coord2address.json")
			.queryParam("x", "127.0")
			.queryParam("y", "37.0")
			.encode(StandardCharsets.UTF_8)
			.build().toUriString();
		String expectedResponse = """
			{
				"meta": {
					"total_count": 1
				},
				"documents": [
					{
						"road_address": {
							"address_name": "경기도 안성시 죽산면 죽산초교길 69-4",
							"region_1depth_name": "경기",
							"region_2depth_name": "안성시",
							"region_3depth_name": "죽산면",
							"road_name": "죽산초교길",
							"building_name": "무지개아파트"
						},
						"address": {
							"address_name": "경기 안성시 죽산면 죽산리 343-1",
							"region_1depth_name": "경기",
							"region_2depth_name": "안성시",
							"region_3depth_name": "죽산면 죽산리"
						}
					}
				]
			}
			""";
		mockServer.expect(requestTo(expectedUri))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON));
		//when
		final KakaoCoordToAddressResponse response = kakaoMapClient.convertCoordToAddress(127.0,
			37.0);

		//then
		assertThat(response).isNotNull();
		assertThat(response.address()).isEqualTo("경기도 안성시 죽산면 죽산초교길 69-4");
	}
}
