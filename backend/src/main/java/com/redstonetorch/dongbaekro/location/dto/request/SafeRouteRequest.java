package com.redstonetorch.dongbaekro.location.dto.request;

import com.redstonetorch.dongbaekro.common.enums.SafetyFacilityType;
import java.util.List;

public record SafeRouteRequest(
	double originLatitude,
	double originLongitude,
	double destinationLatitude,
	double destinationLongitude,
	List<SafetyFacilityType> preferredFacilityTypes
) {
	public String getOriginCoordinates() {
		return originLongitude + "," + originLatitude;
	}

	public String getDestinationCoordinates() {
		return destinationLongitude + "," + destinationLatitude;
	}
}