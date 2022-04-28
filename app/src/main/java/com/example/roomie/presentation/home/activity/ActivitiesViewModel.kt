package com.example.roomie.presentation.home.activity

import androidx.lifecycle.*
import com.example.roomie.core.Resource
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.data.repository.ActivityRepository
import com.example.roomie.domain.model.Activity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val reloadTrigger = MutableLiveData<Boolean>()
    var forceFetch = false

    /**
     * get flatId from shared preferences
     */
    private val flatId: Int = FlatStorage.getFlatId()

    /**
     * Activities of flat as LiveData
     */
    var activities: LiveData<Resource<List<Activity>>> =
        Transformations.switchMap(reloadTrigger) {
            activityRepository.getActivitiesByFlatId(flatId, forceFetch)
        }

    /**
     * refresh on initialization
     */
    init {
        refresh(false)
    }

    /**
     * Trigger reload
     * @param forceFetch determines local or remote reload
     */
    fun refresh(forceFetch: Boolean) {
        this.forceFetch = forceFetch
        reloadTrigger.value = true
    }
}