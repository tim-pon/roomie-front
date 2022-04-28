package com.example.roomie.presentation.authentication

import androidx.lifecycle.ViewModel
import com.example.roomie.data.repository.AuthenticationRepository
import com.example.roomie.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * responsible for preparing data for the UI
 */

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    val repository: AuthenticationRepository
) : ViewModel() {

    fun login(user: User) = repository.login(user)

    fun registration(user: User) = repository.registration(user)
}