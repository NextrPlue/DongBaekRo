package com.redstonetorch.dongbaekro.location.dto.request;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public record WalkingWaypointsRequest(
	Coordinate origin,
	Coordinate destination,
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	List<Coordinate> waypoints,
	String priority,
	Boolean summary,
	Integer defaultSpeed
) {
	public record Coordinate(double x, double y) {}
	
	public static WalkingWaypointsRequest create(
		double originLongitude, double originLatitude,
		double destinationLongitude, double destinationLatitude,
		List<Coordinate> waypoints
	) {
		return new WalkingWaypointsRequest(
			new Coordinate(originLongitude, originLatitude),
			new Coordinate(destinationLongitude, destinationLatitude),
			waypoints != null ? waypoints : new ArrayList<>(),  // null을 빈 배열로 변경
			"DISTANCE",
			false,
			null
		);
	}
}