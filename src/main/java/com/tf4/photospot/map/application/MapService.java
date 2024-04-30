package com.tf4.photospot.map.application;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import com.tf4.photospot.global.util.PointConverter;
import com.tf4.photospot.map.application.response.SearchLocationResponse;
import com.tf4.photospot.map.application.response.kakao.KakaoCoordToAddressResponse;
import com.tf4.photospot.map.application.response.kakao.KakaoDistanceResponse;
import com.tf4.photospot.map.infrastructure.KakaoMapClient;
import com.tf4.photospot.map.infrastructure.KakaoMobilityClient;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapService {
	public static final String NO_SEARCH_ADDRESS = "";

	private final KakaoMapClient kakaoMapClient;
	private final KakaoMobilityClient kakaoMobilityClient;

	public String searchByCoord(Point coord) {
		final KakaoCoordToAddressResponse response = kakaoMapClient.convertCoordToAddress(
			coord.getX(), coord.getY());
		if (response == KakaoCoordToAddressResponse.ERROR_RESPONSE) {
			return NO_SEARCH_ADDRESS;
		}

		return response.address();
	}

	public SearchLocationResponse searchByAddress(String address) {
		if (StringUtils.isBlank(address) || NO_SEARCH_ADDRESS.equals(address)) {
			return SearchLocationResponse.ERROR_RESPONSE;
		}
		return SearchLocationResponse.from(kakaoMapClient.searchAddress(address));
	}

	public Integer searchDistanceBetween(Point startingCoord, Point destCoord) {
		KakaoDistanceResponse response = kakaoMobilityClient.findDistance(
			PointConverter.toStringValue(startingCoord), PointConverter.toStringValue(destCoord));
		return response.getDistance();
	}
}
