package com.tf4.photospot.spot.presentation;

import static java.util.Collections.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.map.application.MapService;
import com.tf4.photospot.mockobject.WithCustomMockUser;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.spot.application.SpotService;
import com.tf4.photospot.spot.application.request.NearbySpotRequest;
import com.tf4.photospot.spot.application.request.RecommendedSpotsRequest;
import com.tf4.photospot.spot.application.response.NearbySpotListResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotListResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotResponse;

@WithCustomMockUser
@WebMvcTest(controllers = SpotController.class)
class SpotControllerTest {
	@MockBean
	private SpotService spotService;

	@MockBean
	private MapService mapService;

	@Autowired
	private MockMvc mockMvc;

	@DisplayName("주변 추천 장소 조회시 미리보기 사진은 1개 이상 10개 이하만 가능하다")
	@MethodSource(value = "postPreviewCountRange")
	@ParameterizedTest(name = "postPreviewCount = {0}")
	void getRecommendedSpotPostPreviewsOutRange(String postPreviewCount, ResultMatcher statusResult) throws Exception {
		//given
		String firstRecommendedSpotAddress = "서울시 도봉구 마들로 640";
		RecommendedSpotListResponse response = RecommendedSpotListResponse.builder()
			.recommendedSpots(List.of(
				createRecommenedSpotResponse(firstRecommendedSpotAddress),
				createRecommenedSpotResponse("서울시 도봉구 마들로 641"),
				createRecommenedSpotResponse("서울시 도봉구 마들로 642"))
			).hasNext(false).build();
		given(mapService.searchByCoord(any(Point.class))).willReturn(firstRecommendedSpotAddress);
		given(spotService.getRecommendedSpotList(any(RecommendedSpotsRequest.class))).willReturn(response);
		mockMvc.perform(get("/api/v1/spots/recommended")
				.queryParam("lon", "127.0")
				.queryParam("lat", "37.0")
				.queryParam("radius", "5000")
				.queryParam("postPreviewCount", postPreviewCount)
			)
			.andDo(print())
			.andExpect(statusResult);
	}

	static Stream<Arguments> postPreviewCountRange() {
		return Stream.of(
			Arguments.of("-1", status().isBadRequest()),
			Arguments.of("0", status().isBadRequest()),
			Arguments.of("11", status().isBadRequest()),
			Arguments.of("1", status().isOk()),
			Arguments.of("10", status().isOk()));
	}

	@DisplayName("주소 정보가 없는데 주변 추천 장소는 있으면 추천 장소의 주소를 전달한다.")
	@Test
	void getRecommendedSpotAddressIfAddressNull() throws Exception {
		//given
		String firstRecommendedSpotAddress = "서울시 도봉구 마들로 640";
		RecommendedSpotListResponse response = RecommendedSpotListResponse.builder()
			.recommendedSpots(List.of(
				createRecommenedSpotResponse(firstRecommendedSpotAddress),
				createRecommenedSpotResponse("서울시 도봉구 마들로 641"),
				createRecommenedSpotResponse("서울시 도봉구 마들로 642"))
			).hasNext(false).build();

		given(mapService.searchByCoord(any(Point.class))).willReturn("");
		given(spotService.getRecommendedSpotList(any(RecommendedSpotsRequest.class))).willReturn(response);

		//when //then
		mockMvc.perform(get("/api/v1/spots/recommended")
				.queryParam("lon", "127.0")
				.queryParam("lat", "37.0")
				.queryParam("radius", "5000")
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.centerAddress").value(firstRecommendedSpotAddress))
			.andExpect(jsonPath("$.recommendedSpots[0].address").value(firstRecommendedSpotAddress));
	}

	private static RecommendedSpotResponse createRecommenedSpotResponse(String address) {
		return RecommendedSpotResponse.builder()
			.address(address)
			.coord(new CoordinateDto(127.0, 37.0))
			.postCount(5L)
			.postPreviewResponses(List.of(
				new PostPreviewResponse(1L, 1L, "profileUrl"),
				new PostPreviewResponse(1L, 1L, "profileUrl"),
				new PostPreviewResponse(1L, 1L, "profileUrl")))
			.build();
	}

	@DisplayName("주변 스팟 장소를 조회한다.")
	@Test
	void getNearbySpots() throws Exception {
		//given
		NearbySpotListResponse response = new NearbySpotListResponse(emptyList());
		given(spotService.getNearbySpotList(any(NearbySpotRequest.class))).willReturn(response);

		//when then
		mockMvc.perform(get("/api/v1/spots")
				.queryParam("lon", "127.0")
				.queryParam("lat", "37.0")
				.queryParam("radius", "5000")
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.spots").isArray());
	}

	@DisplayName("주변 스팟 조회 반경은 0보다 큰 수만 가능하다.")
	@ValueSource(strings = {"-1", "0"})
	@ParameterizedTest(name = "radius = {0}")
	void getNearbySpotsFailToNonPasitiveRadius(String radius) throws Exception {
		//when then
		mockMvc.perform(get("/api/v1/spots")
				.queryParam("lon", "127.0")
				.queryParam("lat", "37.0")
				.queryParam("radius", radius)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
			.andExpect(jsonPath("$.message").value("Invalid Parameter"))
			.andExpect(jsonPath("$.errors[0].value").value(radius))
			.andExpect(jsonPath("$.errors[0].message").value("반경(m)은 0보다 커야 됩니다."));
	}

	@Test
	@DisplayName("기간 조회 시 시작일이 빈 값이어선 안된다.")
	void getSpotsFailWithStartNull() throws Exception {
		String start = null;
		mockMvc.perform(get("/api/v1/spots/mine/period")
				.queryParam("start", start)
				.queryParam("end", "2024-01-07"))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
			.andExpect(jsonPath("$.errors[0].message").value("시작일 또는 종료일에 빈 값이 들어갈 수 없습니다."))
			.andExpect(jsonPath("$.errors[0].field").value("start"));
	}

	@Test
	@DisplayName("기간 조회 시 종료일이 빈 값이어선 안된다.")
	void getSpotsFailWithEndNull() throws Exception {
		String end = null;
		mockMvc.perform(get("/api/v1/spots/mine/period")
				.queryParam("start", "2024-01-01")
				.queryParam("end", end))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
			.andExpect(jsonPath("$.errors[0].message").value("시작일 또는 종료일에 빈 값이 들어갈 수 없습니다."))
			.andExpect(jsonPath("$.errors[0].field").value("end"));
	}

	@Test
	@DisplayName("기간 조회 시 시작일은 종료일 이전이어야 한다.")
	void getSpotsFailWithinInvalidPeriod() throws Exception {
		mockMvc.perform(get("/api/v1/spots/mine/period")
				.queryParam("start", "2024-01-05")
				.queryParam("end", "2024-01-01"))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
			.andExpect(jsonPath("$.errors[0].message").value("시작일은 종료일 이전이어야 합니다."))
			.andExpect(jsonPath("$.errors[0].field").value("start"));
	}

	@Test
	@DisplayName("기간 조회 시 7일을 초과할 수 없다.")
	void getSpotsFailWithinExceededPeriod() throws Exception {
		mockMvc.perform(get("/api/v1/spots/mine/period")
				.queryParam("start", "2024-01-01")
				.queryParam("end", "2024-01-08"))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
			.andExpect(jsonPath("$.errors[0].message").value("선택한 기간이 7일을 초과합니다."))
			.andExpect(jsonPath("$.errors[0].field").value("end"));
	}
}
