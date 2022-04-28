package com.example.roomie.presentation.finance

import androidx.lifecycle.ViewModel
import com.example.roomie.data.repository.GraphRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GraphViewModel @Inject constructor(
    val repository: GraphRepository
): ViewModel() {

    fun getMonthlyExpenses(flatId: Int) = repository.getMonthlyExpenses(flatId)

}