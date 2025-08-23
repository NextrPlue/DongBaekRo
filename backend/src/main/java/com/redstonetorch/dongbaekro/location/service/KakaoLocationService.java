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
import com.redstonetorch.dongbaekro.location.dto.response.KakaoRegionResponse;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoLocationService {

	private final RestTemplate restTemplate;

	@Value("13813d37e731b114145dd1ecb4b8ab59")
	private String kakaoApiKey;

	private static final String KAKAO_COORD_TO_REGION_URL = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json";
	private static final String KAKAO_WALKING_DIRECTIONS_URL = "https://apis-navi.kakaomobility.com/affiliate/walking/v1/directions";

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

			ResponseEntity<KakaoWalkingDirectionsResponse> response = restTemplate.exchange(
				url, HttpMethod.GET, entity, KakaoWalkingDirectionsResponse.class);

			return response.getBody();
		} catch (RestClientException e) {
			log.error("Error calling Kakao Walking Directions API: {}", e.getMessage());
			throw new RuntimeException("카카오 도보 길찾기 API 호출 중 오류가 발생했습니다: " + e.getMessage());
		}
	}
}