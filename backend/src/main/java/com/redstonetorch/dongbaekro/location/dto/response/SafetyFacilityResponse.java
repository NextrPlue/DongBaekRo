package com.redstonetorch.dongbaekro.location.dto.response;

import com.redstonetorch.dongbaekro.common.enums.SafetyFacilityType;
import com.redstonetorch.dongbaekro.location.entity.SafetyFacility;

public record SafetyFacilityResponse(
	Long id,
	SafetyFacilityType type,
	String name,
	Double latitude,
	Double longitude,
	String addressName,
	String region1DepthName,
	String region2DepthName,
	String region3DepthName,
	String code
) {
	public static SafetyFacilityResponse from(SafetyFacility safetyFacility) {
		return new SafetyFacilityResponse(
			safetyFacility.getId(),
			safetyFacility.getType(),
			safetyFacility.getName(),
			safetyFacility.getLatitude(),
			safetyFacility.getLongitude(),
			safetyFacility.getAddress_name(),
			safetyFacility.getRegion_1depth_name(),
			safetyFacility.getRegion_2depth_name(),
			safetyFacility.getRegion_3depth_name(),
			safetyFacility.getCode()
		);
	}
}