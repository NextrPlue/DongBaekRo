package com.redstonetorch.dongbaekro.ui

// 1. 서버 응답을 감싸는 제네릭 클래스
data class ApiResponse<T>(
    val status: String,
    val message: String?,
    val data: T?
)

// 2. 로그인 요청 DTO
data class LoginRequest(
    val email: String,
    val password: String
)

// 3. 로그인 성공 시 `data` 필드에 포함될 DTO
data class LoginData(
    val accessToken: String,
    val refreshToken: String
)

// 4. 회원가입 요청 DTO
data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String
)

// 5. 회원가입 성공 시 `data` 필드에 포함될 DTO
data class SignupData(
    val name: String,
    val email: String
)

// User data class
data class User(
    val id: String, // Assuming a user ID
    val name: String,
    val email: String,
    val phone: String
)

// Request DTO for updating user profile
data class UpdateProfileRequest(
    val name: String,
    val email: String,
    val phone: String
)

// Response DTO for getting user details (if separate API call is needed)
data class GetUserResponse(
    val user: User
)

// SafetyFacility data class
data class SafetyFacility(
    val id: Int,
    val type: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val addressName: String,
    val region1DepthName: String,
    val region2DepthName: String,
    val region3DepthName: String,
    val code: String
)

// Request DTO for safe route API
data class RouteRequest(
    val originLatitude: Double,
    val originLongitude: Double,
    val destinationLatitude: Double,
    val destinationLongitude: Double,
    val preferredFacilityTypes: List<String>
)

// Response DTO for safe route API
data class SafeRouteResponse(
    val status: String,
    val message: String,
    val data: SafeRouteData,
    val timestamp: String
)

data class SafeRouteData(
    val safeRoute: SafeRoute,
    val selectedWaypoints: List<SelectedWaypoint>,
    val comparison: Comparison
)

data class SafeRoute(
    val vertexes: List<Vertex> // List of LatLng pairs
)

data class Vertex(
    val latitude: Double,
    val longitude: Double
)

data class SelectedWaypoint(
    val id: Int,
    val type: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val addressName: String,
    val region1DepthName: String,
    val region2DepthName: String,
    val region3DepthName: String,
    val code: String
)

data class Comparison(
    val duration: Int, // in seconds
    val distance: Int, // in meters
    val waypointCount: Int
)