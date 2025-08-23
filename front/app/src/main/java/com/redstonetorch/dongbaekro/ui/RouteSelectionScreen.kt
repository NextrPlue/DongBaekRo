package com.redstonetorch.dongbaekro.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapView
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.route.RouteLineSegment
import com.kakao.vectormap.route.RouteLineStyle
import com.kakao.vectormap.route.RouteLineStyles
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.redstonetorch.dongbaekro.ui.KakaoRoute
import com.redstonetorch.dongbaekro.ui.Road
import com.redstonetorch.dongbaekro.ui.SafeRouteData
import com.redstonetorch.dongbaekro.ui.SelectedWaypoint

@Composable
fun RouteSelectionScreen(
    viewModel: RouteViewModel = hiltViewModel(),
    origin: LatLng,
    destination: LatLng,
    preferredTypes: List<String>
) {
    val uiState by viewModel.uiState.collectAsState()
    val kakaoMap = remember { mutableStateOf<KakaoMap?>(null) }
    val context = LocalContext.current

    // 화면이 처음 그려질 때 경로 검색 API 호출
    LaunchedEffect(Unit) {
        viewModel.searchSafeRoute(
            origin.latitude, origin.longitude,
            destination.latitude, destination.longitude,
            preferredTypes
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 카카오맵 뷰
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    start(
                        object : MapLifeCycleCallback() {
                            override fun onMapDestroy() {
                                // 지도 API 가 정상적으로 종료될 때 호출됨
                            }
                            override fun onMapError(error: Exception?) {
                                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출됨
                                error?.printStackTrace()
                            }
                        },
                        object : KakaoMapReadyCallback() {
                            override fun onMapReady(map: KakaoMap) {
                                // 인증 후 API 가 정상적으로 실행될 때 호출됨
                                kakaoMap.value = map
                            }
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        when (val state = uiState) {
            is RouteUiState.Idle -> {
                // 아무것도 표시하지 않음
            }
            is RouteUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF3689FF)
                )
            }
            is RouteUiState.Success -> {
                // 경로 데이터가 성공적으로 로드되면 지도에 그리기
                LaunchedEffect(state.data) {
                    kakaoMap.value?.let { map ->
                        try {
                            // 기존 경로선과 마커 모두 제거
                            map.routeLineManager?.layer?.removeAll()
                            map.labelManager?.layer?.removeAll()
                            
                            // 안심 경로선 그리기
                            drawSafeRoute(map, state.data.safeRoute)
                            
                            // 안전시설물 마커 그리기
                            drawWaypoints(map, state.data.selectedWaypoints)
                            
                            // 출발지와 목적지 마커 그리기
                            drawStartEndPoints(map, origin, destination)
                            
                            // 전체 경로가 보이도록 카메라 조정
                            adjustCameraToRoute(map, state.data.safeRoute, origin, destination)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                // 화면 하단 정보 UI
                SafeRouteInfoPanel(
                    routeData = state.data,
                    onStartClick = { 
                        // TODO: 안내 시작 로직
                    }
                )
            }
            is RouteUiState.Error -> {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "경로 검색에 실패했습니다.\n${state.message}",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * 화면 하단에 표시될 '안심 경로' 정보 패널
 */
@Composable
fun BoxScope.SafeRouteInfoPanel(
    routeData: SafeRouteData,
    onStartClick: () -> Unit
) {
    // 안심 경로의 요약 정보 추출
    val comparison = routeData.comparison
    val minutes = comparison.safeDuration / 60
    val distanceKm = String.format("%.1fkm", comparison.safeDistance / 1000.0)
    val facilitiesCount = comparison.safetyFacilitiesCount

    Card(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "최근에 탐색한 경로", 
                fontWeight = FontWeight.Bold, 
                fontSize = 18.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                "약 ${minutes}분 소요 • ${distanceKm}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 안전시설물 정보 표시
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // CCTV 정보
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔍", fontSize = 14.sp)
                    val cctvCount = routeData.selectedWaypoints.count { it.type == "CCTV" }
                    Text(" CCTV ${cctvCount}개", fontSize = 12.sp, color = Color(0xFF666666))
                }
                
                // 가로등 정보  
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💡", fontSize = 14.sp)
                    val streetlightCount = routeData.selectedWaypoints.count { it.type == "streetlight" }
                    Text(" 가로등 ${streetlightCount}개", fontSize = 12.sp, color = Color(0xFF666666))
                }
                
                // 안내반 정보
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("👥", fontSize = 14.sp)
                    val patrolCount = routeData.selectedWaypoints.count { it.type == "patrol" }
                    Text(" 안내반 ${patrolCount}개", fontSize = 12.sp, color = Color(0xFF666666))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 안내 시작 버튼
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3689FF))
            ) {
                Text(
                    "시작", 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = Color.White
                )
            }
        }
    }
}

// 간단한 방식으로 안전시설물 마커를 찍는 함수
private fun drawSimpleWaypoints(map: KakaoMap, waypoints: List<SelectedWaypoint>) {
    try {
        val labelManager = map.labelManager ?: return
        
        waypoints.forEach { waypoint ->
            try {
                // 기본 아이콘으로 단순하게 처리
                val styles = LabelStyles.from(
                    LabelStyle.from(android.R.drawable.ic_dialog_map)
                )
                
                val options = LabelOptions.from(
                    LatLng.from(waypoint.latitude, waypoint.longitude)
                ).setStyles(styles)
                
                // null 체크 추가
                labelManager.layer?.addLabel(options)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// 안심 경로선을 그리는 함수
private fun drawSafeRoute(map: KakaoMap, safeRoute: KakaoRoute) {
    try {
        val routeLineManager = map.routeLineManager ?: return
        val layer = routeLineManager.layer ?: return
        
        // 경로선 스타일 설정 (파란색, 굵기 8dp)
        val routeLineStyle = RouteLineStyle.from(8f, android.graphics.Color.BLUE)
        val routeLineStyles = RouteLineStyles.from(routeLineStyle)
        
        // 모든 roads의 vertexes를 하나의 LatLng 리스트로 변환
        val allLatLngList = mutableListOf<LatLng>()
        
        safeRoute.routes.forEach { route ->
            route.sections.forEach { section ->
                section.roads.forEach { road ->
                    val roadLatLngs = parseVertexes(road)
                    allLatLngList.addAll(roadLatLngs)
                }
            }
        }
        
        android.util.Log.d("RouteSelection", "DrawSafeRoute called with ${allLatLngList.size} total points")
        
        if (allLatLngList.isNotEmpty()) {
            // RouteLineSegment 생성 및 스타일 적용
            val segment = RouteLineSegment.from(allLatLngList).setStyles(routeLineStyles)
            
            // RouteLineOptions 생성 및 추가
            val options = RouteLineOptions.from(segment)
            layer.addRouteLine(options)
            
            android.util.Log.d("RouteSelection", "Route drawn with ${allLatLngList.size} points")
        }
    } catch (e: Exception) {
        android.util.Log.e("RouteSelection", "Error drawing safe route", e)
    }
}

// [lng, lat, lng, lat, ...] 형태의 리스트를 LatLng 리스트로 변환
private fun parseVertexes(road: Road): List<LatLng> {
    return try {
        road.vertexes.chunked(2).mapNotNull { chunk ->
            if (chunk.size == 2) {
                val (lng, lat) = chunk
                LatLng.from(lat, lng)
            } else null
        }
    } catch (e: Exception) {
        android.util.Log.e("RouteSelection", "Error parsing vertexes for road", e)
        emptyList()
    }
}

// 안전시설물 마커를 그리는 함수
private fun drawWaypoints(map: KakaoMap, waypoints: List<SelectedWaypoint>) {
    try {
        val labelManager = map.labelManager ?: return
        val layer = labelManager.layer ?: return
        
        waypoints.forEach { waypoint ->
            try {
                // 시설물 타입별 다른 아이콘 사용
                val iconResource = when (waypoint.type) {
                    "CCTV" -> android.R.drawable.ic_menu_camera
                    "STREETLAMP" -> android.R.drawable.ic_menu_day  
                    "EMERGENCY_BELL" -> android.R.drawable.ic_lock_idle_alarm
                    else -> android.R.drawable.ic_dialog_map
                }
                
                val styles = LabelStyles.from(LabelStyle.from(iconResource))
                val options = LabelOptions.from(
                    LatLng.from(waypoint.latitude, waypoint.longitude)
                ).setStyles(styles)
                
                layer.addLabel(options)
            } catch (e: Exception) {
                android.util.Log.e("RouteSelection", "Error adding waypoint marker: ${waypoint.name}", e)
            }
        }
        
        android.util.Log.d("RouteSelection", "Added ${waypoints.size} waypoint markers")
    } catch (e: Exception) {
        android.util.Log.e("RouteSelection", "Error drawing waypoints", e)
    }
}

// 출발지와 목적지 마커를 그리는 함수
private fun drawStartEndPoints(map: KakaoMap, origin: LatLng, destination: LatLng) {
    try {
        val labelManager = map.labelManager ?: return
        val layer = labelManager.layer ?: return
        
        // 출발지 마커 (초록색)
        val startStyles = LabelStyles.from(LabelStyle.from(android.R.drawable.ic_media_play))
        val startOptions = LabelOptions.from(origin).setStyles(startStyles)
        layer.addLabel(startOptions)
        
        // 목적지 마커 (빨간색)
        val endStyles = LabelStyles.from(LabelStyle.from(android.R.drawable.ic_notification_overlay))
        val endOptions = LabelOptions.from(destination).setStyles(endStyles)
        layer.addLabel(endOptions)
        
        android.util.Log.d("RouteSelection", "Added start and end point markers")
    } catch (e: Exception) {
        android.util.Log.e("RouteSelection", "Error drawing start/end points", e)
    }
}

// 전체 경로가 보이도록 카메라 조정하는 함수
private fun adjustCameraToRoute(
    map: KakaoMap, 
    safeRoute: KakaoRoute,
    origin: LatLng,
    destination: LatLng
) {
    try {
        // 모든 roads의 vertexes에서 좌표 추출
        val allRouteLatitudes = mutableListOf<Double>()
        val allRouteLongitudes = mutableListOf<Double>()
        
        safeRoute.routes.forEach { route ->
            route.sections.forEach { section ->
                section.roads.forEach { road ->
                    road.vertexes.chunked(2).forEach { chunk ->
                        if (chunk.size == 2) {
                            val (lng, lat) = chunk
                            allRouteLatitudes.add(lat)
                            allRouteLongitudes.add(lng)
                        }
                    }
                }
            }
        }
        
        if (allRouteLatitudes.isEmpty()) {
            // 경로 데이터가 없으면 출발지와 목적지만 보이도록 조정
            val midLat = (origin.latitude + destination.latitude) / 2
            val midLng = (origin.longitude + destination.longitude) / 2
            val center = LatLng.from(midLat, midLng)
            map.moveCamera(CameraUpdateFactory.newCenterPosition(center, 14))
            return
        }
        
        // 모든 좌표점들의 경계 계산 (경로 + 출발지 + 목적지)
        val allLatitudes = allRouteLatitudes + listOf(origin.latitude, destination.latitude)
        val allLongitudes = allRouteLongitudes + listOf(origin.longitude, destination.longitude)
        
        val minLat = allLatitudes.minOrNull() ?: origin.latitude
        val maxLat = allLatitudes.maxOrNull() ?: destination.latitude
        val minLng = allLongitudes.minOrNull() ?: origin.longitude
        val maxLng = allLongitudes.maxOrNull() ?: destination.longitude
        
        // 중심점 계산
        val centerLat = (minLat + maxLat) / 2
        val centerLng = (minLng + maxLng) / 2
        val center = LatLng.from(centerLat, centerLng)
        
        // 적절한 줌 레벨 계산 (간단한 방식)
        val latDiff = maxLat - minLat
        val lngDiff = maxLng - minLng
        val maxDiff = maxOf(latDiff, lngDiff)
        
        val zoomLevel = when {
            maxDiff > 0.1 -> 10
            maxDiff > 0.05 -> 12
            maxDiff > 0.01 -> 14
            else -> 16
        }
        
        map.moveCamera(CameraUpdateFactory.newCenterPosition(center, zoomLevel))
        android.util.Log.d("RouteSelection", "Camera adjusted to center: $centerLat, $centerLng with zoom: $zoomLevel")
        
    } catch (e: Exception) {
        android.util.Log.e("RouteSelection", "Error adjusting camera", e)
    }
}

// 간단한 방식으로 출발지와 목적지 마커를 그리는 함수
private fun drawSimpleStartEndPoints(map: KakaoMap, origin: LatLng, destination: LatLng) {
    try {
        val labelManager = map.labelManager ?: return
        
        // 출발지 마커
        val startStyles = LabelStyles.from(
            LabelStyle.from(android.R.drawable.ic_media_play)
        )
        val startOptions = LabelOptions.from(origin).setStyles(startStyles)
        labelManager.layer?.addLabel(startOptions)
        
        // 목적지 마커
        val endStyles = LabelStyles.from(
            LabelStyle.from(android.R.drawable.ic_menu_mylocation)
        )
        val endOptions = LabelOptions.from(destination).setStyles(endStyles)
        labelManager.layer?.addLabel(endOptions)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
