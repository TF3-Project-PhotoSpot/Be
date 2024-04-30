package com.tf4.photospot.map.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.global.util.PointConverter;
import com.tf4.photospot.map.application.response.SearchLocationResponse;
import com.tf4.photospot.map.application.response.kakao.KakaoCoordToAddressResponse;
import com.tf4.photospot.map.application.response.kakao.KakaoSearchLocationResponse;
import com.tf4.photospot.map.domain.TemporaryLocation;
import com.tf4.photospot.map.infrastructure.KakaoMapClient;
import com.tf4.photospot.map.infrastructure.TemporaryLocationRepository;
import com.tf4.photospot.support.IntegrationTestSupport;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class MapServiceTest extends IntegrationTestSupport {
	private final MapService mapService;
	private final TemporaryLocationRepository temporaryLocationRepository;

	@MockBean
	private final KakaoMapClient kakaoMapClient;

	@DisplayName("좌표로 주소를 찾을 수 없으면 NO_SEARCH_ADDRESS가 반환된다.")
	@Test
	void failSearchByCoord() {
		//given
		final Point coord = PointConverter.convert(new CoordinateDto(127.0, 37.0));
		given(kakaoMapClient.convertCoordToAddress(anyDouble(), anyDouble()))
			.willReturn(KakaoCoordToAddressResponse.ERROR_RESPONSE);
		//when
		final String result = mapService.searchByCoord(coord);

		//then
		assertThat(result).isEqualTo(MapService.NO_SEARCH_ADDRESS);
	}

	@DisplayName("주소가 유효하지 않으면 ERROR_RESPONSE가 반환된다.")
	@Test
	void searchByInvalidAddress() {
		final SearchLocationResponse result = mapService.searchByAddress(MapService.NO_SEARCH_ADDRESS);

		//then
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(SearchLocationResponse.ERROR_RESPONSE);
	}

	@DisplayName("주소로 검색한 결과가 없으면 ERROR_RESPONSE가 반환된다.")
	@Test
	void failSearchByAddress() {
		given(kakaoMapClient.searchAddress(anyString())).willReturn(KakaoSearchLocationResponse.ERROR_RESPONSE);

		final SearchLocationResponse result = mapService.searchByAddress("도봉구 마들로");

		assertThat(result).isEqualTo(SearchLocationResponse.ERROR_RESPONSE);
	}

	@DisplayName("임시 장소를 생성한다.")
	@Test
	void createTemporaryLocation() {
		//given
		final double lat = 127.0;
		final double lon = 37.0;
		//when
		mapService.createTemporaryLocation(new CoordinateDto(lon, lat));
		//then
		final Optional<TemporaryLocation> result = temporaryLocationRepository.findAll().stream()
			.filter(temporaryLocation -> temporaryLocation.getLat() == lat && temporaryLocation.getLon() == lon)
			.findAny();
		assertThat(result).isPresent();
	}
}
