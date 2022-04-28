package com.example.roomie.presentation.finance.overview

import androidx.lifecycle.*
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.data.repository.TransactionRepository
import com.example.roomie.domain.model.TransactionWithCreatorAndUsers
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    // Trigger value to reload transactions (used for swipeOnRefresh layout)
    private val reloadTrigger = MutableLiveData<Boolean>()
    var forceFetch = false

    private val flatId = FlatStorage.getFlatId()

    val transactionsWithCreatorAndUsers = Transformations.switchMap(reloadTrigger) {
        transactionRepository.getTransactionsWithCreatorAndUsersByFlatId(flatId, forceFetch) }


    init { refresh(false) }

    fun refresh(forceFetch: Boolean) {
        this.forceFetch = forceFetch
        reloadTrigger.value = true
    }

    fun deleteTransaction(transactionWithCreatorAndUsers: TransactionWithCreatorAndUsers) =
        transactionRepository.deleteTransaction(transactionWithCreatorAndUsers)

}