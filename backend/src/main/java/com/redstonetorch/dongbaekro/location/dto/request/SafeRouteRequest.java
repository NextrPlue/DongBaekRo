package com.redstonetorch.dongbaekro.location.dto.request;

public record SafeRouteRequest(
	double originLatitude,
	double originLongitude,
	double destinationLatitude,
	double destinationLongitude
) {
	public String getOriginCoordinates() {
		return originLongitude + "," + originLatitude;
	}

	public String getDestinationCoordinates() {
		return destinationLongitude + "," + destinationLatitude;
	}
}