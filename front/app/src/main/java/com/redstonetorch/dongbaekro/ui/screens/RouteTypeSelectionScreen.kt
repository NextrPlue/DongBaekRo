package com.redstonetorch.dongbaekro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.redstonetorch.dongbaekro.R
import com.redstonetorch.dongbaekro.ui.dto.KakaoPlace

@Composable
fun RouteTypeSelectionScreen(
    currentLat: Double,
    currentLng: Double,
    destination: KakaoPlace,
    onRouteTypeSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val kakaoMap = remember { mutableStateOf<KakaoMap?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 카카오맵 배경
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    start(
                        object : MapLifeCycleCallback() {
                            override fun onMapDestroy() {}
                            override fun onMapError(e: Exception?) {}
                        },
                        object : KakaoMapReadyCallback() {
                            override fun onMapReady(map: KakaoMap) {
                                kakaoMap.value = map
                                setupRouteSelectionMap(map, currentLat, currentLng, destination)
                            }
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 상단 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .zIndex(2f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_navigate_before_24),
                    contentDescription = "뒤로가기",
                    tint = Color(0xFF6B7280)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "목적지",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = destination.place_name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151)
                    )
                    Text(
                        text = destination.road_address_name.ifEmpty { destination.address_name },
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF),
                        maxLines = 1
                    )
                }
            }
        }

        // 하단 경로 유형 선택 카드
        RouteTypeSelectionBottomCard(
            onRouteTypeSelected = onRouteTypeSelected,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun RouteTypeSelectionBottomCard(
    onRouteTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 핸들 바
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(4.dp)
                    .background(Color(0xFFDEDEDE), RoundedCornerShape(2.dp))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "안심 경로 유형을 선택하세요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "선택한 안전시설물을 우선적으로 경유하는 경로를 안내합니다",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 경로 유형 선택 버튼들
            RouteTypeButton(
                icon = "📹",
                title = "CCTV 우선 경로",
                description = "CCTV가 많이 설치된 경로를 우선 안내",
                color = Color(0xFFF3F4F6),
                onClick = { onRouteTypeSelected("CCTV") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            RouteTypeButton(
                icon = "🔔",
                title = "비상벨 우선 경로",
                description = "비상벨이 설치된 구간을 우선 안내",
                color = Color(0xFFFEF3C7),
                onClick = { onRouteTypeSelected("EMERGENCY_BELL") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            RouteTypeButton(
                icon = "💡",
                title = "가로등 우선 경로",
                description = "가로등이 충분한 밝은 길을 우선 안내",
                color = Color(0xFFDCFCE7),
                onClick = { onRouteTypeSelected("STREETLAMP") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            RouteTypeButton(
                icon = "🛡️",
                title = "종합 안심 경로",
                description = "모든 안전시설물을 고려한 최적의 경로",
                color = Color(0xFFDDD6FE),
                onClick = { onRouteTypeSelected("COMPREHENSIVE") }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RouteTypeButton(
    icon: String,
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF374151)
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.baseline_navigate_before_24),
                contentDescription = "선택",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// 경로 선택 화면용 지도 설정
private fun setupRouteSelectionMap(
    map: KakaoMap, 
    currentLat: Double, 
    currentLng: Double, 
    destination: KakaoPlace
) {
    try {
        val labelManager = map.labelManager ?: return
        
        // 현재 위치 마커
        val currentPos = LatLng.from(currentLat, currentLng)
        val currentLocationStyles = com.kakao.vectormap.label.LabelStyles.from(
            com.kakao.vectormap.label.LabelStyle.from(android.R.drawable.presence_online)
        )
        val currentLocationOptions = LabelOptions.from(currentPos).setStyles(currentLocationStyles)
        labelManager.layer?.addLabel(currentLocationOptions)
        
        // 목적지 마커
        val destinationPos = LatLng.from(destination.y.toDouble(), destination.x.toDouble())
        val destinationStyles = com.kakao.vectormap.label.LabelStyles.from(
            com.kakao.vectormap.label.LabelStyle.from(android.R.drawable.ic_notification_overlay)
        )
        val destinationOptions = LabelOptions.from(destinationPos).setStyles(destinationStyles)
        labelManager.layer?.addLabel(destinationOptions)
        
        // 두 지점이 모두 보이도록 카메라 조정
        val midLat = (currentLat + destination.y.toDouble()) / 2
        val midLng = (currentLng + destination.x.toDouble()) / 2
        val midPoint = LatLng.from(midLat, midLng)
        
        map.moveCamera(CameraUpdateFactory.newCenterPosition(midPoint, 13))
        
    } catch (e: Exception) {
        android.util.Log.e("RouteTypeSelection", "Error setting up map", e)
    }
}