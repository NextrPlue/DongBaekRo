package com.redstonetorch.dongbaekro.ui

import androidx.lifecycle.ViewModel
import com.redstonetorch.dongbaekro.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class NeighborhoodViewModel @Inject constructor() : ViewModel() {

    val tags = listOf(
        TagItem("병원", R.drawable.ic_board),
        TagItem("약국", R.drawable.ic_chat),
        TagItem("미용", R.drawable.ic_feed),
        TagItem("카페", R.drawable.ic_gallery),
    )

    private val _selectedTag = MutableStateFlow(tags.first())
    val selectedTag: StateFlow<TagItem> = _selectedTag

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet

    private val _showThanksDialog = MutableStateFlow(false)
    val showThanksDialog: StateFlow<Boolean> = _showThanksDialog

    private val _markers = MutableStateFlow<List<Pair<Double, Double>>>(emptyList())
    val markers: StateFlow<List<Pair<Double, Double>>> = _markers

    private val _showAdoptConfirm = MutableStateFlow(false)
    val showAdoptConfirm: StateFlow<Boolean> = _showAdoptConfirm

    fun selectTag(tag: TagItem) {
        _selectedTag.value = tag
    }

    fun searchPlaces(query: String, lat: Double, lng: Double) {
        // TODO: Implement actual search logic
    }

    fun hideBottomSheet() {
        _showBottomSheet.value = false
    }

    fun setThanksDialog(show: Boolean) {
        _showThanksDialog.value = show
    }

    fun setShowAdoptConfirm(show: Boolean) {
        _showAdoptConfirm.value = show
    }
}
