package com.redstonetorch.dongbaekro.location.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

import com.redstonetorch.dongbaekro.location.dto.response.KakaoWalkingDirectionsResponse;
import com.redstonetorch.dongbaekro.location.dto.request.WalkingDirectionsRequest;
import com.redstonetorch.dongbaekro.location.dto.request.WalkingWaypointsRequest;
import com.redstonetorch.dongbaekro.location.dto.response.KakaoRegionResponse;

@Slf4j
@Service
public class KakaoLocationService {

	private final RestTemplate restTemplate;
	private final String kakaoApiKey;

	public KakaoLocationService(RestTemplate restTemplate, @Value("${KAKAO_REST_API}") String kakaoApiKey) {
		this.restTemplate = restTemplate;
		this.kakaoApiKey = kakaoApiKey;
	}

	private static final String KAKAO_COORD_TO_REGION_URL = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json";
	private static final String KAKAO_WALKING_DIRECTIONS_URL = "https://apis-navi.kakaomobility.com/affiliate/walking/v1/directions";
	private static final String KAKAO_WALKING_WAYPOINTS_URL = "https://apis-navi.kakaomobility.com/affiliate/walking/v1/waypoints/directions";

	public String getRegionCodeFromCoordinates(double longitude, double latitude) {
		String url = UriComponentsBuilder.fromHttpUrl(KAKAO_COORD_TO_REGION_URL)
			.queryParam("x", longitude)
			.queryParam("y", latitude)
			.build()
			.toUriString();

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "KakaoAK " + kakaoApiKey);

		HttpEntity<Void> entity = new HttpEntity<>(headers);

		ResponseEntity<KakaoRegionResponse> response = restTemplate.exchange(
			url, HttpMethod.GET, entity, KakaoRegionResponse.class);

		KakaoRegionResponse responseBody = response.getBody();
		if (responseBody != null && responseBody.documents() != null) {
			return responseBody.documents().stream()
				.filter(document -> "H".equals(document.regionType()))
				.map(KakaoRegionResponse.Document::code)
				.findFirst()
				.orElse(null);
		}

		return null;
	}

	public KakaoWalkingDirectionsResponse getWalkingDirections(WalkingDirectionsRequest request) {
		try {
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(KAKAO_WALKING_DIRECTIONS_URL)
				.queryParam("origin", request.getOriginCoordinates())
				.queryParam("destination", request.getDestinationCoordinates());

			if (request.waypoints() != null && !request.waypoints().isEmpty()) {
				builder.queryParam("waypoints", request.waypoints());
			}
			if (request.priority() != null) {
				builder.queryParam("priority", request.priority());
			}
			if (request.summary() != null) {
				builder.queryParam("summary", request.summary());
			}
			if (request.defaultSpeed() != null) {
				builder.queryParam("default_speed", request.defaultSpeed());
			}

			String url = builder.build().toUriString();
			log.info("Kakao Walking Directions API URL: {}", url);

			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "KakaoAK " + kakaoApiKey);
			headers.set("Content-Type", "application/json");
			headers.set("Accept", "application/json");
			headers.set("service", "dongbaekro");

			HttpEntity<Void> entity = new HttpEntity<>(headers);

			// 디버깅을 위해 먼저 String으로 응답 확인
			ResponseEntity<String> rawResponse = restTemplate.exchange(
				url, HttpMethod.GET, entity, String.class);

			log.info("Raw Kakao API Response: {}", rawResponse.getBody());

			ResponseEntity<KakaoWalkingDirectionsResponse> response = restTemplate.exchange(
				url, HttpMethod.GET, entity, KakaoWalkingDirectionsResponse.class);

			return response.getBody();
		} catch (RestClientException e) {
			log.error("Error calling Kakao Walking Directions API: {}", e.getMessage());
			throw new RuntimeException("카카오 도보 길찾기 API 호출 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	public KakaoWalkingDirectionsResponse getWalkingDirectionsWithWaypoints(WalkingWaypointsRequest request) {
		try {
			log.info("Calling Kakao Walking Waypoints API with origin: {}, destination: {}, waypoints: {}",
				request.origin(), request.destination(), request.waypoints() != null ? request.waypoints().size() : 0);
			log.info("Request body: {}", request);

			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "KakaoAK " + kakaoApiKey);
			headers.set("Content-Type", "application/json");
			headers.set("Accept", "application/json");
			headers.set("service", "dongbaekro");

			HttpEntity<WalkingWaypointsRequest> entity = new HttpEntity<>(request, headers);
			log.info("Request headers: {}", headers);
			log.info("Request entity: {}", entity.getBody());

			// 디버깅을 위해 먼저 String으로 응답 확인
			ResponseEntity<String> rawResponse = restTemplate.exchange(
				KAKAO_WALKING_WAYPOINTS_URL, HttpMethod.POST, entity, String.class);

			log.info("Raw Kakao Waypoints API Response: {}", rawResponse.getBody());

			ResponseEntity<KakaoWalkingDirectionsResponse> response = restTemplate.exchange(
				KAKAO_WALKING_WAYPOINTS_URL, HttpMethod.POST, entity, KakaoWalkingDirectionsResponse.class);

			return response.getBody();
		} catch (RestClientException e) {
			log.error("Error calling Kakao Walking Waypoints API: {}", e.getMessage());
			throw new RuntimeException("카카오 경유지 도보 길찾기 API 호출 중 오류가 발생했습니다: " + e.getMessage());
		}
	}
}