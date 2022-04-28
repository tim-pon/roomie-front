package com.example.roomie.presentation.finance.statistics

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import com.example.roomie.data.repository.TransactionRepository
import com.example.roomie.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val repository: TransactionRepository,
    val userRepository: UserRepository
) : ViewModel(){

    var isEnabled: ObservableBoolean? = null

    fun getFinanceStatistics(flatId: Int) = repository.getFinanceStatistics(flatId)

    fun settleFinance(flatId: Int) = repository.settleFinance(flatId)

    fun getUser(userId: Int) = userRepository.getUserById(userId)

    init {
        isEnabled = ObservableBoolean(false)
    }
}