package com.example.roomie.presentation.settings

import androidx.lifecycle.*
import com.example.roomie.data.repository.SettingsRepository
import com.example.roomie.domain.model.ChangePassword
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val repository: SettingsRepository
) : ViewModel() {

    fun getUser() = repository.getUser()

    fun getFlatsFromUser() = repository.getFlatsFromUser()

    fun getFlatInfo(flatId: Int) = repository.getFlatInfo(flatId)

    fun changeUsername(newUsername: String) = repository.changeUsername(newUsername)

    fun changePassword(changePassword: ChangePassword) = repository.changePassword(changePassword)

    fun uploadImage(image: MultipartBody.Part) = repository.uploadImage(image)

}