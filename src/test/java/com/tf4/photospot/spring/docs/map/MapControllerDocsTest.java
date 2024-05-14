package com.tf4.photospot.spring.docs.map;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.restdocs.payload.JsonFieldType;

import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.map.application.MapService;
import com.tf4.photospot.map.application.response.SearchLocationResponse;
import com.tf4.photospot.map.presentation.MapController;
import com.tf4.photospot.spring.docs.RestDocsSupport;

class MapControllerDocsTest extends RestDocsSupport {
	private final MapService mapService = mock(MapService.class);

	@Override
	protected Object initController() {
		return new MapController(mapService);
	}

	@DisplayName("특정 좌표로 지도의 장소를 찾는다.")
	@Test
	void findRegisteredSpot() throws Exception {
		//given
		given(mapService.searchByCoord(any(Point.class))).willReturn("전북 익산시 부송동 100");
		given(mapService.searchByAddress(anyString())).willReturn(SearchLocationResponse.builder()
			.address("전북 익산시 부송동 100")
			.coord(new CoordinateDto(126.99, 35.97))
			.build());
		//when then
		mockMvc.perform(get("/api/v1/map/search/location")
				.queryParam("lat", "35.97")
				.queryParam("lon", "126.99"))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				queryParameters(
					parameterWithName("lat").description("위도").attributes(coordConstraints()),
					parameterWithName("lon").description("경도").attributes(coordConstraints())
				),
				responseFields(
					fieldWithPath("address").type(JsonFieldType.STRING).description("지번 주소"),
					fieldWithPath("coord.lat").type(JsonFieldType.NUMBER).description("지번 주소 위도"),
					fieldWithPath("coord.lon").type(JsonFieldType.NUMBER).description("지번 주소 경도")
				)));
	}
}
