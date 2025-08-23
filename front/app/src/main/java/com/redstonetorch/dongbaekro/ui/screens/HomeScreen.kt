package com.redstonetorch.dongbaekro.ui.screens

import android.Manifest
import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelTextBuilder
import com.redstonetorch.dongbaekro.R
import com.redstonetorch.dongbaekro.ui.NeighborhoodViewModel
import com.redstonetorch.dongbaekro.ui.SearchViewModel
import com.redstonetorch.dongbaekro.ui.dto.KakaoPlace
import com.redstonetorch.dongbaekro.util.CommonUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onNavigateToRouteSelection: (Double, Double, KakaoPlace) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val viewModel: NeighborhoodViewModel = hiltViewModel()
    val searchViewModel: SearchViewModel = hiltViewModel()
    
    // 선택된 장소 상태
    var selectedDestination by remember { mutableStateOf<KakaoPlace?>(null) }

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    var currentLat by remember { mutableStateOf<Double?>(null) }
    var currentLng by remember { mutableStateOf<Double?>(null) }

    // 검색 관련 상태
    var searchText by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // 지도 관련 상태
    val mapView = remember { mutableStateOf<MapView?>(null) }
    val kakaoMap = remember { mutableStateOf<KakaoMap?>(null) }

    // 바텀시트 상태 - 원래 구조 복원
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()

    // Collect safety facilities from ViewModel
    val safetyFacilities by viewModel.safetyFacilities.collectAsState()
    // Collect region code from ViewModel
    val regionCode by viewModel.regionCode.collectAsState()

    LaunchedEffect(locationPermissionState) {
        if (locationPermissionState.status.isGranted) {
            val location = CommonUtils.getXY(context)
            if (location != null) {
                currentLat = location.first
                currentLng = location.second
                // Fetch region code when location is available
                viewModel.getRegionCode(location.first, location.second)
            }
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }

    // Observe safetyFacilities and update map markers
    LaunchedEffect(safetyFacilities) {
        kakaoMap.value?.let { map ->
            // Clear all existing labels (pins) except the user's location marker
            map.labelManager?.layer?.removeAll()
            currentLat?.let { lat -> currentLng?.let { lng -> setupUserLocationMarker(map, lat, lng) } }

            // Add selected destination marker if available
            selectedDestination?.let { destination ->
                addDestinationMarker(map, destination)
            }

            // Add new labels for safety facilities
            safetyFacilities.forEach { facility ->
                val pos = LatLng.from(facility.latitude, facility.longitude)
                val labelOptions = LabelOptions.from(pos)
                    .setStyles(getMarkerStyleForType(facility.type))

                map.labelManager?.layer?.addLabel(labelOptions)
            }

            // Move camera to the first fetched facility if available
            safetyFacilities.firstOrNull()?.let { firstFacility ->
                val firstFacilityPos = LatLng.from(firstFacility.latitude, firstFacility.longitude)
                map.moveCamera(CameraUpdateFactory.newCenterPosition(firstFacilityPos, 15))
            }
        }
    }
    
    // Observe selected destination and update map
    LaunchedEffect(selectedDestination) {
        selectedDestination?.let { destination ->
            kakaoMap.value?.let { map ->
                // Clear existing markers
                map.labelManager?.layer?.removeAll()
                
                // Add user location marker
                currentLat?.let { lat -> 
                    currentLng?.let { lng -> 
                        setupUserLocationMarker(map, lat, lng) 
                    } 
                }
                
                // Add destination marker and move camera
                addDestinationMarker(map, destination)
                moveToDestination(map, destination)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // BottomSheetScaffold가 메인
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 32.dp, // 1. 완전히 내렸을 때 작은 바만 보이도록
            sheetMaxWidth = BottomSheetDefaults.SheetMaxWidth,
            sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            sheetContainerColor = Color.White,
            sheetContent = {
                DongbaekroBottomSheetContent(
                    onSafetyFacilityClick = { facilityType ->
                        // 2. 안심 시설물 클릭 시 바텀시트 완전히 내리기
                        scope.launch {
                            scaffoldState.bottomSheetState.partialExpand()
                        }

                        // Call ViewModel to fetch safety facilities
                        viewModel.getSafetyFacilities(facilityType)
                    }
                )
            },
        ) { innerPadding ->
            // 지도 영역 (원래대로 복원)
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (locationPermissionState.status.isGranted) {
                    if (currentLat != null && currentLng != null) {
                        AndroidView(
                            factory = { ctx ->
                                MapView(ctx).apply {
                                    mapView.value = this
                                    start(
                                        object : MapLifeCycleCallback() {
                                            override fun onMapDestroy() {}
                                            override fun onMapError(e: Exception?) {}
                                            override fun onMapResumed() {}
                                        },
                                        object : KakaoMapReadyCallback() {
                                            override fun onMapReady(map: KakaoMap) {
                                                kakaoMap.value = map
                                                setupUserLocationMarker(map, currentLat!!, currentLng!!)
                                            }
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "위치 권한이 필요합니다.")
                    }
                }
            }
        }

        // 상단 검색 바와 내 위치 버튼 (지도 위 오버레이)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .zIndex(2f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 검색 바
                TopSearchBar(
                    searchText = searchText,
                    onSearchTextChange = { searchText = it },
                    isSearchActive = isSearchActive,
                    onSearchActiveChange = { isSearchActive = it },
                    focusRequester = focusRequester,
                    modifier = Modifier.weight(1f)
                )

                // 내 위치로 돌아가기 버튼
                FloatingActionButton(
                    onClick = {
                        currentLat?.let { lat ->
                            currentLng?.let { lng ->
                                moveToMyLocation(kakaoMap.value, lat, lng)
                            }
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    containerColor = Color.White,
                    contentColor = Color(0xFF55758A)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.my_location_24px),
                        contentDescription = "내 위치",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 검색 활성화 시 전체 화면 검색 UI
        if (isSearchActive) {
            SearchOverlay(
                searchText = searchText,
                onSearchTextChange = { searchText = it },
                onSearchClose = {
                    isSearchActive = false
                    keyboardController?.hide()
                },
                onSearchSubmit = { query ->
                    searchViewModel.searchPlaces(query, currentLat, currentLng)
                },
                onPlaceSelected = { place ->
                    selectedDestination = place
                    searchText = place.place_name
                    isSearchActive = false
                    keyboardController?.hide()
                    
                    // 경로 선택 화면으로 네비게이션
                    currentLat?.let { lat ->
                        currentLng?.let { lng ->
                            onNavigateToRouteSelection(lat, lng, place)
                        }
                    }
                },
                focusRequester = focusRequester,
                currentLat = currentLat,
                currentLng = currentLng,
                searchViewModel = searchViewModel
            )
        }
    }
}

@Composable
fun TopSearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(12.dp)
            .clickable {
                onSearchActiveChange(true)
            }
    ) {
//        // 동백로 로고
//        Icon(
//            painter = painterResource(id = R.drawable.dbr),
//            contentDescription = "동백로",
//            tint = Color(0xFFFFFFFF),
//            modifier = Modifier.size(24.dp)
//        )

        Spacer(modifier = Modifier.width(8.dp))

        // 검색 입력창
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFFF8F9FA), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            // 빨간 원점
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFFEF4444), RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (searchText.isNotEmpty()) searchText else "장소를 입력하세요",
                color = if (searchText.isNotEmpty()) Color(0xFF374151) else Color(0xFF6B7280),
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 검색 아이콘
        Icon(
            painter = painterResource(id = R.drawable.outline_search_24),
            contentDescription = "검색",
            tint = Color(0xFF6B7280),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SearchOverlay(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearchClose: () -> Unit,
    onSearchSubmit: (String) -> Unit,
    onPlaceSelected: (KakaoPlace) -> Unit,
    focusRequester: FocusRequester,
    currentLat: Double?,
    currentLng: Double?,
    searchViewModel: SearchViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var startLocation by remember { mutableStateOf("내 위치") }
    
    // 검색 결과 상태
    val searchResults by searchViewModel.searchResults.collectAsState()
    val isLoading by searchViewModel.isLoading.collectAsState()
    val searchError by searchViewModel.error.collectAsState()

    // 실제 위치를 주소로 변환
    LaunchedEffect(currentLat, currentLng) {
        if (currentLat != null && currentLng != null) {
            try {
                val address = getAddressFromLocation(context, currentLat, currentLng)
                startLocation = "내 위치 : $address"
            } catch (e: Exception) {
                startLocation = "내 위치 : 위치 확인 중..."
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .zIndex(10f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 앱 로고 헤더
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                IconButton(
                    onClick = onSearchClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_navigate_before_24),
                        contentDescription = "뒤로가기",
                        tint = Color(0xFF6B7280)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))


            }

            // 출발지 입력창
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9FA), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    // 초록 원점 (출발지)
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF10B981), RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    OutlinedTextField(
                        value = startLocation,
                        onValueChange = { startLocation = it },
                        placeholder = {
                            Text(
                                text = "출발지를 입력하세요",
                                color = Color(0xFF6B7280)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        ),
                        singleLine = true
                    )

                    // 내 위치 아이콘
                    Icon(
                        painter = painterResource(id = R.drawable.location_on),
                        contentDescription = "내 위치",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                if (currentLat != null && currentLng != null) {
                                    // 코루틴에서 현재 위치 다시 설정
                                    scope.launch {
                                        try {
                                            val address = getAddressFromLocation(context, currentLat, currentLng)
                                            startLocation = "내 위치 : $address"
                                        } catch (e: Exception) {
                                            startLocation = "내 위치 : 위치 확인 중..."
                                        }
                                    }
                                } else {
                                    startLocation = "내 위치 : 위치 확인 중..."
                                }
                            }
                    )
                }
            }

            // 목적지 입력창
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9FA), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    // 빨간 원점 (목적지)
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFEF4444), RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    OutlinedTextField(
                        value = searchText,
                        onValueChange = onSearchTextChange,
                        placeholder = {
                            Text(
                                text = "목적지를 입력하세요",
                                color = Color(0xFF6B7280)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (searchText.isNotEmpty()) {
                                    onSearchSubmit(searchText)
                                }
                            }
                        ),
                        trailingIcon = {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        ),
                        singleLine = true
                    )

                    // 검색 아이콘
                    Icon(
                        painter = painterResource(id = R.drawable.outline_search_24),
                        contentDescription = "검색",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                if (searchText.isNotEmpty()) {
                                    onSearchSubmit(searchText)
                                }
                            }
                    )
                }
            }


            // 검색 결과 목록
            Column {
                // 에러 메시지 표시
                searchError?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(12.dp),
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp
                        )
                    }
                }
                
                // 검색 결과 표시
                if (searchResults.isNotEmpty()) {
                    Text(
                        text = "검색 결과",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    searchResults.forEach { place ->
                        SearchResultItem(
                            place = place,
                            onClick = { selectedPlace ->
                                onPlaceSelected(selectedPlace)
                                searchViewModel.clearResults()
                            }
                        )
                    }
                } else if (searchText.isEmpty()) {
                    // 기본 추천 장소들 (검색어가 없을 때만 표시)
                    Text(
                        text = "추천 장소",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    SearchCategory(
                        icon = R.drawable.location_on,
                        title = "조개토굴이",
                        onClick = { onSearchTextChange("조개토굴이") }
                    )
                    SearchCategory(
                        icon = R.drawable.outline_search_24,
                        title = "조개구이",
                        onClick = { onSearchTextChange("조개구이") }
                    )
                    SearchCategory(
                        icon = R.drawable.location_on,
                        title = "투인사브 산동점",
                        onClick = { onSearchTextChange("투인사브 산동점") }
                    )
                    SearchCategory(
                        icon = R.drawable.location_on,
                        title = "공자 구리구평점",
                        onClick = { onSearchTextChange("공자 구리구평점") }
                    )
                }
            }
        }
    }

    // 검색어 변경 시 자동 검색 (디바운싱)
    LaunchedEffect(searchText) {
        if (searchText.isNotEmpty()) {
            kotlinx.coroutines.delay(300) // 300ms 디바운싱
            searchViewModel.searchPlaces(searchText, currentLat, currentLng)
        } else {
            searchViewModel.clearResults()
        }
    }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

// 좌표를 주소로 변환하는 함수
suspend fun getAddressFromLocation(context: Context, latitude: Double, longitude: Double): String {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                // 한국 주소 형식에 맞게 구성
                val addressParts = listOfNotNull(
                    address.thoroughfare, // 도로명
                    address.subThoroughfare, // 상세주소 (건물번호 등)
                    address.subLocality // 동/읍/면
                ).filter { it.isNotBlank() }

                if (addressParts.isNotEmpty()) {
                    addressParts.joinToString(" ")
                } else {
                    // fallback: 시/구 정보라도 보여주기
                    listOfNotNull(
                        address.locality, // 시
                        address.subAdminArea // 구
                    ).joinToString(" ").ifEmpty { "현재 위치" }
                }
            } else {
                "현재 위치"
            }
        } catch (e: Exception) {
            "현재 위치"
        }
    }
}

@Composable
fun CategoryButton(
    icon: Int,
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { /* TODO: 카테고리별 경로 검색 */ },
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = text,
                tint = Color(0xFF374151),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 10.sp,
                color = Color(0xFF374151)
            )
        }
    }
}

@Composable
fun SearchResultItem(
    place: KakaoPlace,
    onClick: (KakaoPlace) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(place) }
            .padding(vertical = 12.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.location_on),
            contentDescription = "장소",
            tint = Color(0xFF3B82F6),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = place.place_name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151)
            )
            Text(
                text = place.road_address_name.ifEmpty { place.address_name },
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 2.dp)
            )
            if (place.category_group_name.isNotEmpty()) {
                Text(
                    text = place.category_group_name,
                    fontSize = 10.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }

        if (place.distance.isNotEmpty()) {
            Text(
                text = "${(place.distance.toIntOrNull() ?: 0)}m",
                fontSize = 10.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
fun SearchCategory(
    icon: Int,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick?.invoke() }
            .padding(vertical = 8.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = title,
            tint = Color(0xFF6B7280),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color(0xFF374151)
            )
            subtitle?.let {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
fun DongbaekroBottomSheetContent(
    onSafetyFacilityClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight() // 내용에 맞게만 높이 설정
            .padding(16.dp)
    ) {
        // 핸들 바
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFDEDEDE))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 최근 방문한 경로 카드 (항상 보임)
        RecentRouteCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 안심 시설 섹션 (바텀시트를 올리면 보임)
        SafetyFacilitiesSection(onSafetyFacilityClick)

        // 적당한 여백만 추가
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun RecentRouteCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "최근에 방문한 경로",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "약 12분 소요 • 1.2km",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                Button(
                    onClick = { /* 시작 버튼 클릭 */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0086CD)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "시작",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SafetyTag(
                    icon = "🟢",
                    text = "CCTV 15개",
                    backgroundColor = Color(0xFFDCFCE7)
                )
                SafetyTag(
                    icon = "💡",
                    text = "가로등 충분",
                    backgroundColor = Color(0xFFFEF3C7)
                )
                SafetyTag(
                    icon = "🔺",
                    text = "안내판 있음",
                    backgroundColor = Color(0xFFFFE4E1)
                )
            }
        }
    }
}

@Composable
fun SafetyTag(
    icon: String,
    text: String,
    backgroundColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = icon,
            fontSize = 12.sp,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(
            text = text,
            fontSize = 10.sp,
            color = Color(0xFF374151)
        )
    }
}

@Composable
fun SafetyFacilitiesSection(onSafetyFacilityClick: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "안심 시설물",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SafetyFacilityButton(
                icon = "📦",
                text = "안심택배함",
                onClick = { onSafetyFacilityClick("SAFE_DELIVERY_BOX") },
                modifier = Modifier.weight(1f)
            )
            SafetyFacilityButton(
                icon = "🏢",
                text = "파출소", // Changed text to 파출소
                onClick = { onSafetyFacilityClick("POLICE_SUBSTATION") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SafetyFacilityButton(
                icon = "📹",
                text = "CCTV",
                onClick = { onSafetyFacilityClick("CCTV") },
                modifier = Modifier.weight(1f)
            )
            SafetyFacilityButton(
                icon = "💡",
                text = "가로등", // Changed text to 가로등
                onClick = { onSafetyFacilityClick("STREETLAMP") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SafetyFacilityButton(
                icon = "🪧", // Placeholder icon for INFORMATION_BOARD
                text = "안내판",
                onClick = { onSafetyFacilityClick("INFORMATION_BOARD") },
                modifier = Modifier.weight(1f)
            )
            SafetyFacilityButton(
                icon = "🔔", // Placeholder icon for EMERGENCY_BELL
                text = "비상벨",
                onClick = { onSafetyFacilityClick("EMERGENCY_BELL") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SafetyFacilityButton(
    icon: String,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = icon,
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                text = text,
                fontSize = 14.sp,
                color = Color(0xFF374151),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// 사용자 위치 마커 설정
fun setupUserLocationMarker(map: KakaoMap, lat: Double, lng: Double) {
    val pos = LatLng.from(lat, lng)

    map.labelManager?.layer?.addLabel(
        LabelOptions.from(pos)
            .setStyles(android.R.drawable.presence_online)
    )

    map.moveCamera(
        CameraUpdateFactory.newCenterPosition(pos, 15)
    )
}

// 내 위치로 돌아가는 기능
fun moveToMyLocation(map: KakaoMap?, lat: Double, lng: Double) {
    map?.let {
        val pos = LatLng.from(lat, lng)
        it.moveCamera(
            CameraUpdateFactory.newCenterPosition(pos, 15)
        )
    }
}

// 목적지 마커 추가 함수
fun addDestinationMarker(map: KakaoMap, destination: KakaoPlace) {
    try {
        val labelManager = map.labelManager ?: return
        val destinationLatLng = LatLng.from(destination.y.toDouble(), destination.x.toDouble())
        
        val destinationStyles = com.kakao.vectormap.label.LabelStyles.from(
            com.kakao.vectormap.label.LabelStyle.from(android.R.drawable.ic_notification_overlay)
        )
        
        val destinationOptions = LabelOptions.from(destinationLatLng)
            .setStyles(destinationStyles)
        
        labelManager.layer?.addLabel(destinationOptions)
    } catch (e: Exception) {
        android.util.Log.e("HomeScreen", "Error adding destination marker", e)
    }
}

// 목적지로 카메라 이동 함수
fun moveToDestination(map: KakaoMap, destination: KakaoPlace) {
    try {
        val destinationLatLng = LatLng.from(destination.y.toDouble(), destination.x.toDouble())
        map.moveCamera(CameraUpdateFactory.newCenterPosition(destinationLatLng, 15))
    } catch (e: Exception) {
        android.util.Log.e("HomeScreen", "Error moving camera to destination", e)
    }
}

// Helper function to get marker style based on facility type
fun getMarkerStyleForType(type: String): com.kakao.vectormap.label.LabelStyles {
    val iconRes = when (type) {
        "CCTV" -> android.R.drawable.ic_menu_mylocation // More visible placeholder
        "STREETLAMP" -> android.R.drawable.ic_menu_mylocation // More visible placeholder
        "POLICE_SUBSTATION" -> android.R.drawable.ic_menu_mylocation // More visible placeholder
        "INFORMATION_BOARD" -> android.R.drawable.ic_menu_mylocation // More visible placeholder
        "EMERGENCY_BELL" -> android.R.drawable.ic_menu_mylocation // More visible placeholder
        "SAFE_DELIVERY_BOX" -> android.R.drawable.ic_menu_mylocation // More visible placeholder
        else -> android.R.drawable.ic_menu_mylocation // Default marker
    }
    return com.kakao.vectormap.label.LabelStyles.from(
        com.kakao.vectormap.label.LabelStyle.from(iconRes)
    )
}

