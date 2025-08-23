package com.redstonetorch.dongbaekro.location.dto.response;

import java.util.List;

public record SafeRouteResponse(
	KakaoWalkingDirectionsResponse originalRoute,
	List<SafetyFacilityResponse> selectedWaypoints,
	KakaoWalkingDirectionsResponse safeRoute,
	RouteComparison comparison
) {
	public record RouteComparison(
		int originalDistance,
		int safeDistance,
		int originalDuration,
		int safeDuration,
		int additionalDistance,
		int additionalDuration,
		int safetyFacilitiesCount
	) {
	}
}