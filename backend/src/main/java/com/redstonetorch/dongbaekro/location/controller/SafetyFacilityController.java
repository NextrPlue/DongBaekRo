package com.redstonetorch.dongbaekro.location.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.redstonetorch.dongbaekro.common.dto.response.ApiResponse;
import com.redstonetorch.dongbaekro.location.dto.response.SafetyFacilityResponse;
import com.redstonetorch.dongbaekro.location.service.KakaoLocationService;
import com.redstonetorch.dongbaekro.location.service.SafetyFacilityService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/safety-facilities")
@RequiredArgsConstructor
public class SafetyFacilityController {

	private final SafetyFacilityService safetyFacilityService;
	private final KakaoLocationService kakaoLocationService;

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
}