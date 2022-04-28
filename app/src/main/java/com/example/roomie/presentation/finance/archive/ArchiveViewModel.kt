package com.example.roomie.presentation.finance.archive

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val reloadTrigger = MutableLiveData<Boolean>()
    var forceFetch = false

    /**
     * get flatId from shared preferences
     */
    private val flatId = FlatStorage.getFlatId()

    /**
     * transactions including information about their creators and users of flat as LiveData
     */
    val transactionsWithCreatorAndUsers = Transformations.switchMap(reloadTrigger) {
        transactionRepository.getTransactionsWithCreatorAndUsersByFlatId(flatId, forceFetch) }


    /**
     * refresh on initialization
     */
    init { refresh(false) }

    /**
     * Trigger reload
     * @param forceFetch determines local or remote reload
     */
    fun refresh(forceFetch: Boolean) {
        this.forceFetch = forceFetch
        reloadTrigger.value = true
    }
}