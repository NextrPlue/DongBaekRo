package com.redstonetorch.dongbaekro.location.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.redstonetorch.dongbaekro.location.dto.response.KakaoRegionResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KakaoLocationService {

	private final RestTemplate restTemplate;

	@Value("13813d37e731b114145dd1ecb4b8ab59")
	private String kakaoApiKey;

	private static final String KAKAO_COORD_TO_REGION_URL = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json";

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
}