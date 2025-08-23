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
                            // 간단한 마커만 추가 (복잡한 경로 그리기는 제외)
                            drawSimpleWaypoints(map, state.data.selectedWaypoints)
                            drawSimpleStartEndPoints(map, origin, destination)
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
    val minutes = comparison.duration / 60
    val distanceKm = String.format("%.1fkm", comparison.distance / 1000.0)
    val facilitiesCount = comparison.waypointCount

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
