package com.redstonetorch.dongbaekro.ui.dto

import com.google.gson.annotations.SerializedName

// API 응답의 최상위 공통 구조
data class ApiResponse<T>(
    val status: String,
    val message: String,
    val data: T?,
    val timestamp: String
)

// 길찾기 결과의 'data' 필드에 해당하는 전체 데이터
data class RouteResultData(
    val originalRoute: Route,
    val selectedWaypoints: List<Waypoint>,
    val safeRoute: Route,
    val comparison: Comparison
)

// originalRoute와 safeRoute의 구조
data class Route(
    @SerializedName("trans_id")
    val transId: String,
    val routes: List<RouteDetail>
)

data class RouteDetail(
    @SerializedName("result_code")
    val resultCode: Int,
    @SerializedName("result_message")
    val resultMessage: String,
    val summary: RouteSummary,
    val sections: List<RouteSection>
)

data class RouteSummary(
    val distance: Int, // 미터 단위
    val duration: Int  // 초 단위
)

data class RouteSection(
    val distance: Int,
    val duration: Int,
    val roads: List<Road>
)

data class Road(
    val distance: Int,
    val duration: Int,
    val vertexes: List<Double> // [lng1, lat1, lng2, lat2, ...] 형태로 제공
)

// 경유하는 안전시설물 정보
data class Waypoint(
    val id: Int,
    val type: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val addressName: String
)

// 경로 비교 정보
data class Comparison(
    val originalDistance: Int,
    val safeDistance: Int,
    val originalDuration: Int,
    val safeDuration: Int,
    val additionalDistance: Int,
    val additionalDuration: Int,
    val safetyFacilitiesCount: Int
)