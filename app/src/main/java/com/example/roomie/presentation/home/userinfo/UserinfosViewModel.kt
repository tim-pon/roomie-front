package com.example.roomie.presentation.home.userinfo

import androidx.lifecycle.*
import com.example.roomie.core.Resource
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.data.repository.FlatRepository
import com.example.roomie.data.repository.UserRepository
import com.example.roomie.domain.model.Flat
import com.example.roomie.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserinfosViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val flatRepository: FlatRepository
) : ViewModel() {

    private val reloadTrigger = MutableLiveData<Boolean>()
    var forceFetch = false

    /**
     * get flatId and corresponding flatName from shared preferences
     */
    private val flatId: Int = FlatStorage.getFlatId()
    val flatName: String = FlatStorage.getFlatName()!!

    var flatInfo: LiveData<Resource<Flat>> =
        Transformations.switchMap(reloadTrigger) {
            flatRepository.getFlatInfo(flatId, forceFetch)
        }

    /**
     * userinfos of flat as LiveData
     */
    var userinfos: LiveData<Resource<List<User>>> =
        Transformations.switchMap(reloadTrigger) {
            userRepository.getUserInfosByFlatId(flatId, forceFetch)
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