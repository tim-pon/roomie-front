package com.example.roomie.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.roomie.core.map
import com.example.roomie.data.repository.NetworkStatusTracker
import kotlinx.coroutines.Dispatchers

sealed class MyState {
    object Fetched : MyState()
    object Error : MyState()
}

class NetworkStatusViewModel(
    networkStatusTracker: NetworkStatusTracker,
) : ViewModel() {

    val state =
        networkStatusTracker.networkStatus
            .map(
                onUnavailable = { MyState.Error },
                onAvailable = { MyState.Fetched },
            )
            .asLiveData(Dispatchers.IO)
}