package com.tf4.photospot.map.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.global.util.PointConverter;
import com.tf4.photospot.map.application.MapService;
import com.tf4.photospot.map.application.response.SearchLocationResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api/v1/map")
@RestController
@RequiredArgsConstructor
public class MapController {
	private static final String TEMPORARY_LOCATION_ADDRESS = "";

	private final MapService mapService;

	@GetMapping("/search/location")
	public SearchLocationResponse searchLocation(
		@ModelAttribute @Valid CoordinateDto coord
	) {
		final String address = mapService.searchByCoord(PointConverter.convert(coord));
		final SearchLocationResponse searchLocationResponse = mapService.searchByAddress(address);
		if (searchLocationResponse == SearchLocationResponse.ERROR_RESPONSE) {
			mapService.createTemporaryLocation(coord);
			return new SearchLocationResponse(TEMPORARY_LOCATION_ADDRESS, coord);
		}
		return searchLocationResponse;
	}

}
