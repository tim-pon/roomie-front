package com.example.roomie.presentation.finance.overview

import androidx.lifecycle.*
import com.example.roomie.core.Resource
import com.example.roomie.data.repository.FlatRepository
import com.example.roomie.data.repository.TransactionRepository
import com.example.roomie.domain.model.FlatWithUsers
import com.example.roomie.domain.model.TransactionWithCreatorAndUsers
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DialogViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    flatRepository: FlatRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionId = savedStateHandle.get<Int>("transactionId")!!
    private val flatId = savedStateHandle.get<Int>("flatId")!!
    private val transactionName = savedStateHandle.get<String>("transactionName")

    var transaction: LiveData<Resource<TransactionWithCreatorAndUsers>> = if (transactionId == -1) {
        val emptyTransaction = TransactionWithCreatorAndUsers.init(flatId)
        if (transactionName !== null)
            emptyTransaction.transactionWithCreator.transaction.name = transactionName
        MutableLiveData(Resource.loading(emptyTransaction))
    } else
        transactionRepository.getTransactionWithCreatorAndUsersById(transactionId)

    val flatUser = flatRepository.getUsersByFlatId(flatId)


    // Merged livedata of transaction and flat users.
    // Required to exec functions which require both data to be loaded successful
    val dependentLiveData = MergedLiveData(sourceA = transaction, sourceB = flatUser) {
            _: Resource<TransactionWithCreatorAndUsers>?,
            _: Resource<FlatWithUsers>? ->
    }

    fun updateTransaction() =
        if (transaction.value?.data?.transactionWithCreator?.transaction?.id!! == -1)
            transactionRepository.createTransactionWithCreatorAndUsers(transaction.value?.data!!)
        else
            transactionRepository.setTransaction(transaction.value?.data!!)


    class MergedLiveData<T, K, S>(sourceA: LiveData<T>, sourceB: LiveData<K>, private val mergedCallback: (resultA: T?, resultB: K?) -> S) : MediatorLiveData<S>() {

        private var resultA: T? = null
        private var resultB: K? = null

        init {
            super.addSource(sourceA) {
                resultA = it
                value = mergedCallback(resultA, resultB)
            }
            super.addSource(sourceB) {
                resultB = it
                value = mergedCallback(resultA, resultB)
            }
        }
    }
}