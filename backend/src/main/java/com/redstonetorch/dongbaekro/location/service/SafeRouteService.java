package com.redstonetorch.dongbaekro.location.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.redstonetorch.dongbaekro.common.enums.SafetyFacilityType;
import com.redstonetorch.dongbaekro.location.dto.response.KakaoWalkingDirectionsResponse;
import com.redstonetorch.dongbaekro.location.dto.request.SafeRouteRequest;
import com.redstonetorch.dongbaekro.location.dto.request.WalkingWaypointsRequest;
import com.redstonetorch.dongbaekro.location.dto.response.SafeRouteResponse;
import com.redstonetorch.dongbaekro.location.dto.response.SafetyFacilityResponse;
import com.redstonetorch.dongbaekro.location.entity.SafetyFacility;
import com.redstonetorch.dongbaekro.location.repository.SafetyFacilityRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SafeRouteService {

	private final KakaoLocationService kakaoLocationService;
	private final SafetyFacilityRepository safetyFacilityRepository;

	private static final double SEARCH_RADIUS_METERS = 100.0;
	private static final int MAX_WAYPOINTS = 15; // 카카오 API 제한

	public SafeRouteResponse generateSafeRoute(SafeRouteRequest request) {
		// 1. 기본 경로 정보 조회
		KakaoWalkingDirectionsResponse originalRoute = getOriginalRoute(request);

		// 2. 경로 상의 안전시설 조회
		List<SafetyFacility> nearbyFacilities = findNearbyFacilities(originalRoute, request);

		// 3. 적절한 경유지 선택
		List<SafetyFacility> selectedWaypoints = selectOptimalWaypoints(
			originalRoute, nearbyFacilities, request
		);

		// 4. 안전 경로 생성
		KakaoWalkingDirectionsResponse safeRoute = null;
		if (!selectedWaypoints.isEmpty()) {
			safeRoute = generateRouteWithWaypoints(request, selectedWaypoints);
		}

		// 5. 응답 생성
		return createSafeRouteResponse(originalRoute, selectedWaypoints, safeRoute);
	}

	private KakaoWalkingDirectionsResponse getOriginalRoute(SafeRouteRequest request) {
		// 경유지 없는 기본 경로를 waypoints API로 요청
		WalkingWaypointsRequest waypointsRequest = WalkingWaypointsRequest.create(
			request.originLongitude(), request.originLatitude(),
			request.destinationLongitude(), request.destinationLatitude(),
			null  // 경유지 없음
		);

		KakaoWalkingDirectionsResponse response = kakaoLocationService.getWalkingDirectionsWithWaypoints(
			waypointsRequest);

		// 디버깅용 로그 추가
		if (response != null && response.routes() != null && !response.routes().isEmpty()) {
			var route = response.routes().get(0);
			log.info("Original route: distance={}, sections={}",
				route.summary().distance(),
				route.sections() != null ? route.sections().size() : "null");
		}

		return response;
	}

	private List<SafetyFacility> findNearbyFacilities(KakaoWalkingDirectionsResponse route, SafeRouteRequest request) {
		List<SafetyFacility> allFacilities = new ArrayList<>();

		if (route.routes() == null || route.routes().isEmpty()) {
			return allFacilities;
		}

		var firstRoute = route.routes().get(0);
		if (firstRoute.sections() == null) {
			return allFacilities;
		}

		// 경로의 모든 좌표점에서 주변 안전시설 검색
		for (var section : firstRoute.sections()) {
			if (section.roads() != null) {
				for (var road : section.roads()) {
					if (road.vertexes() != null && road.vertexes().size() >= 2) {
						// vertexes는 [lng, lat, lng, lat, ...] 형태의 평면 배열
						for (int i = 0; i < road.vertexes().size() - 1; i += 2) {
							double longitude = road.vertexes().get(i);
							double latitude = road.vertexes().get(i + 1);

							List<SafetyFacility> facilities = safetyFacilityRepository
								.findFacilitiesWithinRadius(latitude, longitude, SEARCH_RADIUS_METERS);

							// 중복 제거를 위해 ID 기반으로 체크하고, 선호 시설 타입으로 필터링
							for (SafetyFacility facility : facilities) {
								if (allFacilities.stream().noneMatch(f -> f.getId().equals(facility.getId())) &&
									isPreferredFacilityType(facility, request.preferredFacilityTypes())) {
									allFacilities.add(facility);
								}
							}
						}
					}
				}
			}
		}

		log.info("Found {} safety facilities within {}m of the route matching preferred types", allFacilities.size(),
			SEARCH_RADIUS_METERS);
		return allFacilities;
	}

	private List<SafetyFacility> selectOptimalWaypoints(
		KakaoWalkingDirectionsResponse route, List<SafetyFacility> facilities, SafeRouteRequest request
	) {
		if (facilities.isEmpty() || route.routes().isEmpty()) {
			return new ArrayList<>();
		}

		int totalDistance = route.routes().get(0).summary().distance();
		int optimalWaypointCount = calculateOptimalWaypointCount(totalDistance);

		if (optimalWaypointCount == 0) {
			return new ArrayList<>();
		}

		// 경로를 구간으로 나누어 각 구간에서 가장 적절한 시설 선택
		List<SafetyFacility> selectedWaypoints = new ArrayList<>();
		List<Double[]> routeCoordinates = extractRouteCoordinates(route);

		if (routeCoordinates.isEmpty()) {
			return new ArrayList<>();
		}

		double totalRouteLength = calculateRouteLength(routeCoordinates);
		double segmentLength = totalRouteLength / (optimalWaypointCount + 1);

		for (int i = 1; i <= optimalWaypointCount; i++) {
			double targetDistance = segmentLength * i;
			Double[] targetPoint = getPointAtDistance(routeCoordinates, targetDistance);

			if (targetPoint != null) {
				SafetyFacility nearestFacility = findNearestFacility(facilities, targetPoint[1], targetPoint[0]);
				if (nearestFacility != null && !selectedWaypoints.contains(nearestFacility)) {
					selectedWaypoints.add(nearestFacility);
				}
			}
		}

		log.info("Selected {} waypoints from {} facilities", selectedWaypoints.size(), facilities.size());
		return selectedWaypoints;
	}

	private int calculateOptimalWaypointCount(int distanceMeters) {
		// 거리에 따른 적절한 경유지 개수 계산
		if (distanceMeters < 500)
			return 2;        // 500m 미만: 경유지 없음
		if (distanceMeters < 1000)
			return 4;       // 1km 미만: 1개
		if (distanceMeters < 2000)
			return 6;       // 2km 미만: 2개
		if (distanceMeters < 3000)
			return 8;       // 3km 미만: 3개
		if (distanceMeters < 5000)
			return 10;       // 5km 미만: 4개
		return Math.min(MAX_WAYPOINTS, 15);         // 5km 이상: 최대 5개
	}

	private List<Double[]> extractRouteCoordinates(KakaoWalkingDirectionsResponse route) {
		List<Double[]> coordinates = new ArrayList<>();

		for (var section : route.routes().get(0).sections()) {
			if (section.roads() != null) {
				for (var road : section.roads()) {
					if (road.vertexes() != null && road.vertexes().size() >= 2) {
						for (int i = 0; i < road.vertexes().size() - 1; i += 2) {
							coordinates.add(new Double[] {road.vertexes().get(i), road.vertexes().get(i + 1)});
						}
					}
				}
			}
		}

		return coordinates;
	}

	private double calculateRouteLength(List<Double[]> coordinates) {
		double totalLength = 0.0;
		for (int i = 1; i < coordinates.size(); i++) {
			totalLength += calculateDistance(
				coordinates.get(i - 1)[1], coordinates.get(i - 1)[0],
				coordinates.get(i)[1], coordinates.get(i)[0]
			);
		}
		return totalLength;
	}

	private Double[] getPointAtDistance(List<Double[]> coordinates, double targetDistance) {
		double accumulatedDistance = 0.0;

		for (int i = 1; i < coordinates.size(); i++) {
			Double[] prev = coordinates.get(i - 1);
			Double[] current = coordinates.get(i);

			double segmentDistance = calculateDistance(prev[1], prev[0], current[1], current[0]);

			if (accumulatedDistance + segmentDistance >= targetDistance) {
				// 목표 지점이 이 구간에 있음
				double ratio = (targetDistance - accumulatedDistance) / segmentDistance;
				double lat = prev[1] + (current[1] - prev[1]) * ratio;
				double lng = prev[0] + (current[0] - prev[0]) * ratio;
				return new Double[] {lng, lat};
			}

			accumulatedDistance += segmentDistance;
		}

		return coordinates.isEmpty() ? null : coordinates.get(coordinates.size() - 1);
	}

	private SafetyFacility findNearestFacility(List<SafetyFacility> facilities, double latitude, double longitude) {
		SafetyFacility nearest = null;
		double minDistance = Double.MAX_VALUE;

		for (SafetyFacility facility : facilities) {
			double distance = calculateDistance(latitude, longitude, facility.getLatitude(), facility.getLongitude());
			if (distance < minDistance) {
				minDistance = distance;
				nearest = facility;
			}
		}

		return nearest;
	}

	private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
		// Haversine formula
		double R = 6371000; // 지구 반지름 (미터)
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
			Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
				Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return R * c;
	}

	private KakaoWalkingDirectionsResponse generateRouteWithWaypoints(
		SafeRouteRequest request, List<SafetyFacility> waypoints
	) {
		if (waypoints.isEmpty()) {
			return null;
		}

		// 경유지를 좌표 배열로 변환
		List<WalkingWaypointsRequest.Coordinate> waypointCoords = new ArrayList<>();
		for (int i = 0; i < Math.min(waypoints.size(), MAX_WAYPOINTS); i++) {
			SafetyFacility facility = waypoints.get(i);
			waypointCoords.add(new WalkingWaypointsRequest.Coordinate(
				facility.getLongitude(), facility.getLatitude()
			));
		}

		WalkingWaypointsRequest waypointsRequest = WalkingWaypointsRequest.create(
			request.originLongitude(), request.originLatitude(),
			request.destinationLongitude(), request.destinationLatitude(),
			waypointCoords
		);

		return kakaoLocationService.getWalkingDirectionsWithWaypoints(waypointsRequest);
	}

	private SafeRouteResponse createSafeRouteResponse(
		KakaoWalkingDirectionsResponse originalRoute,
		List<SafetyFacility> selectedWaypoints,
		KakaoWalkingDirectionsResponse safeRoute
	) {
		List<SafetyFacilityResponse> waypointResponses = selectedWaypoints.stream()
			.map(SafetyFacilityResponse::from)
			.toList();

		SafeRouteResponse.RouteComparison comparison = null;
		if (safeRoute != null && !safeRoute.routes().isEmpty() && !originalRoute.routes().isEmpty()) {
			var originalSummary = originalRoute.routes().get(0).summary();
			var safeSummary = safeRoute.routes().get(0).summary();

			comparison = new SafeRouteResponse.RouteComparison(
				originalSummary.distance(),
				safeSummary.distance(),
				originalSummary.duration(),
				safeSummary.duration(),
				safeSummary.distance() - originalSummary.distance(),
				safeSummary.duration() - originalSummary.duration(),
				selectedWaypoints.size()
			);
		}

		return new SafeRouteResponse(originalRoute, waypointResponses, safeRoute, comparison);
	}

	private boolean isPreferredFacilityType(SafetyFacility facility, List<SafetyFacilityType> preferredTypes) {
		// 선호 타입이 지정되지 않았다면 모든 시설 허용
		if (preferredTypes == null || preferredTypes.isEmpty()) {
			return true;
		}
		// 시설이 선호 타입 목록에 포함되는지 확인
		return preferredTypes.contains(facility.getType());
	}
}