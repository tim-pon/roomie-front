package com.example.roomie.presentation.flat

import androidx.lifecycle.ViewModel
import com.example.roomie.data.repository.FlatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FlatViewModel @Inject constructor(
    private val repository: FlatRepository
) : ViewModel() {

    fun joinFlat(entryCode: String) = repository.joinFlat(entryCode)

    fun createFlat(flatName: String) = repository.createFlat(flatName)

    fun leaveFlat(flatId: Int) = repository.leaveFlat(flatId)
}