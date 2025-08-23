package com.redstonetorch.dongbaekro.location.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.redstonetorch.dongbaekro.common.dto.response.ApiResponse;
import com.redstonetorch.dongbaekro.common.enums.SafetyFacilityType;
import com.redstonetorch.dongbaekro.location.dto.request.SafeRouteRequest;
import com.redstonetorch.dongbaekro.location.dto.response.KakaoWalkingDirectionsResponse;
import com.redstonetorch.dongbaekro.location.dto.request.WalkingDirectionsRequest;
import com.redstonetorch.dongbaekro.location.dto.response.SafeRouteResponse;
import com.redstonetorch.dongbaekro.location.dto.response.SafetyFacilityResponse;
import com.redstonetorch.dongbaekro.location.service.KakaoLocationService;
import com.redstonetorch.dongbaekro.location.service.SafeRouteService;
import com.redstonetorch.dongbaekro.location.service.SafetyFacilityService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/safety-facilities")
@RequiredArgsConstructor
public class SafetyFacilityController {

	private final SafetyFacilityService safetyFacilityService;
	private final KakaoLocationService kakaoLocationService;
	private final SafeRouteService safeRouteService;

	@GetMapping("/by-code")
	public ResponseEntity<ApiResponse<List<SafetyFacilityResponse>>> getSafetyFacilitiesByCode(
		@RequestParam String code) {
		List<SafetyFacilityResponse> facilities = safetyFacilityService.findByCode(code);
		return ResponseEntity.ok(ApiResponse.success(facilities));
	}

	@GetMapping("/region-code")
	public ResponseEntity<ApiResponse<String>> getRegionCodeFromCoordinates(
		@RequestParam double latitude,
		@RequestParam double longitude) {
		String regionCode = kakaoLocationService.getRegionCodeFromCoordinates(longitude, latitude);
		return ResponseEntity.ok(ApiResponse.success(regionCode));
	}

	@GetMapping("/walking-directions")
	public ResponseEntity<ApiResponse<KakaoWalkingDirectionsResponse>> getWalkingDirections(
		@RequestParam double originLatitude,
		@RequestParam double originLongitude,
		@RequestParam double destinationLatitude,
		@RequestParam double destinationLongitude,
		@RequestParam(required = false) String waypoints,
		@RequestParam(required = false) String priority,
		@RequestParam(required = false) Boolean summary,
		@RequestParam(required = false) Integer defaultSpeed) {

		try {
			WalkingDirectionsRequest request = new WalkingDirectionsRequest(
				originLongitude, originLatitude,
				destinationLongitude, destinationLatitude,
				waypoints, priority, summary, defaultSpeed
			);

			KakaoWalkingDirectionsResponse directions = kakaoLocationService.getWalkingDirections(request);
			return ResponseEntity.ok(ApiResponse.success(directions));
		} catch (RuntimeException e) {
			return ResponseEntity.internalServerError()
				.body(ApiResponse.error(e.getMessage(), null));
		}
	}

	@GetMapping("/safe-route")
	public ResponseEntity<ApiResponse<SafeRouteResponse>> generateSafeRoute(
		@RequestParam double originLatitude,
		@RequestParam double originLongitude,
		@RequestParam double destinationLatitude,
		@RequestParam double destinationLongitude) {

		try {
			SafeRouteRequest request = new SafeRouteRequest(
				originLatitude, originLongitude,
				destinationLatitude, destinationLongitude
			);

			SafeRouteResponse safeRoute = safeRouteService.generateSafeRoute(request);
			return ResponseEntity.ok(ApiResponse.success(safeRoute));
		} catch (RuntimeException e) {
			return ResponseEntity.internalServerError()
				.body(ApiResponse.error(e.getMessage(), null));
		}
	}
}