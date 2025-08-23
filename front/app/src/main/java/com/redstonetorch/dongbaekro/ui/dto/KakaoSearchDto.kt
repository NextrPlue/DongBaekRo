package com.redstonetorch.dongbaekro.ui.dto

data class KakaoSearchResponse(
    val documents: List<KakaoPlace>,
    val meta: KakaoSearchMeta
)

data class KakaoPlace(
    val id: String,
    val place_name: String,
    val address_name: String,
    val road_address_name: String,
    val x: String,  // 경도
    val y: String,  // 위도
    val phone: String,
    val category_name: String,
    val category_group_code: String,
    val category_group_name: String,
    val distance: String,
    val place_url: String
)

data class KakaoSearchMeta(
    val total_count: Int,
    val pageable_count: Int,
    val is_end: Boolean,
    val same_name: KakaoSameName?
)

data class KakaoSameName(
    val region: List<String>,
    val keyword: String,
    val selected_region: String
)