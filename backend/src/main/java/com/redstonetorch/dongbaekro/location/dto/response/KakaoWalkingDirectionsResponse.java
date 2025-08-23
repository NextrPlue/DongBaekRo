package com.redstonetorch.dongbaekro.location.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoWalkingDirectionsResponse(
	@JsonProperty("trans_id")
	String transId,
	List<Route> routes
) {
	public record Route(
		@JsonProperty("result_code")
		int resultCode,
		@JsonProperty("result_message")
		String resultMessage,
		Summary summary,
		List<Section> sections
	) {
	}

	public record Summary(
		int distance,
		int duration
	) {
	}

	public record Section(
		int distance,
		int duration,
		List<Road> roads
	) {
	}

	public record Road(
		int distance,
		int duration,
		List<Double> vertexes
	) {
	}
}