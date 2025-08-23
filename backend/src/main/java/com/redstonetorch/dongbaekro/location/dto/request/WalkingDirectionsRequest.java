package com.redstonetorch.dongbaekro.location.dto.request;

public record WalkingDirectionsRequest(
	double originLongitude,
	double originLatitude,
	double destinationLongitude,
	double destinationLatitude,
	String waypoints,
	String priority,
	Boolean summary,
	Integer defaultSpeed
) {
	public String getOriginCoordinates() {
		return originLongitude + "," + originLatitude;
	}

	public String getDestinationCoordinates() {
		return destinationLongitude + "," + destinationLatitude;
	}
}