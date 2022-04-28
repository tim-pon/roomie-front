package com.example.roomie.presentation.authentication

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.roomie.R
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.core.sharedpreferences.SecureStorage
import com.example.roomie.core.Status
import com.example.roomie.databinding.FragmentRegistrationBinding
import com.example.roomie.domain.model.User
import com.example.roomie.presentation.CustomSnackbar
import com.example.roomie.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegistrationFragment : Fragment() {

    /**
     * @param binding to more easily write code that interacts with view
     * @param navController manages app navigation from one fragment to another
     * @param viewModel to store and manage UI-related data in a lifecycle conscious way for authentication
     */
    private lateinit var binding: FragmentRegistrationBinding
    private lateinit var navController: NavController
    private val viewModel: AuthenticationViewModel by viewModels()

    /**
     *  inflate the layout of the fragmen
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegistrationBinding.inflate(layoutInflater)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.registration_title)
        MainActivity.navView.visibility = View.INVISIBLE
        navController = findNavController()

        binding.buttonRegistration.isEnabled = false
        /**
         * button function to register an user
         */
        binding.buttonRegistration.setOnClickListener { registration() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**
         * to observe multiplie textinput fields at the same time
         */
        val editTexts = listOf(binding.mailInput,
            binding.passwordInput,
            binding.usernameInput,
            binding.matchingPasswordInput)

        /**
         * checks if the input of the textinput fields are valid or not
         * if so then enable the button Login so that the user can log in
         */
        for (editText in editTexts) {
            editText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    inputValidation()
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                }

                override fun afterTextChanged(s: Editable) {}
            })
        }

        /**
         * displays a error message to the user after leaving the input field
         */
        binding.mailInput.setOnFocusChangeListener { v, hasFocus ->
            val mail = binding.mailInput.text.toString()
            if (!hasFocus) {
                if (!isUserNameValid(mail)) {
                    binding.mail.error = getString(R.string.invalid_email_address)
                } else {
                    binding.mail.error = null
                }
            } else {
                if (isUserNameValid(mail)) {
                    binding.mail.error = null
                }
            }
        }

        /**
         * displays a error message to the user after leaving the input field
         */
        binding.mailInput.setOnFocusChangeListener { _, hasFocus ->
            val mail = binding.mailInput.text.toString()
            if (!hasFocus) {
                if (!isMailValid(mail)) {
                    binding.mail.error = getString(R.string.invalid_email_address)
                } else {
                    binding.mail.error = null
                }
            } else {
                if (isMailValid(mail)) {
                    binding.mail.error = null
                }
            }
        }

        /**
         * displays a error message to the user after leaving the input field
         */
        binding.passwordInput.setOnFocusChangeListener { _, hasFocus ->
            val password = binding.passwordInput.text.toString()
            if (!hasFocus) {
                if (!isPasswordValid(password)) {
                    binding.password.error = getString(R.string.invalid_password)
                } else {
                    binding.password.error = null
                }
            } else {
                if (isPasswordValid(password)) {
                    binding.password.error = null
                }
            }
        }

        /**
         * displays a error message to the user after leaving the input field
         */
        binding.usernameInput.setOnFocusChangeListener { _, hasFocus ->
            val username = binding.usernameInput.text.toString()
            if (!hasFocus) {
                if (!isUserNameValid(username)) {
                    binding.username.error = getString(R.string.invalid_username)
                } else {
                    binding.username.error = null
                }
            } else {
                if (isUserNameValid(username)) {
                    binding.username.error = null
                }
            }
        }

        /**
         * displays a error message to the user after leaving the input field
         */
        binding.matchingPasswordInput.setOnFocusChangeListener { _, hasFocus ->
            val matchingPassword = binding.matchingPasswordInput.text.toString()
            val password = binding.passwordInput.text.toString()
            if (!hasFocus) {
                if (!arePasswordMatching(matchingPassword, password)) {
                    binding.matchingPassword.error = getString(R.string.error_matching_password)
                } else {
                    binding.matchingPassword.error = null
                }
            } else {
                if (arePasswordMatching(matchingPassword, password)) {
                    binding.matchingPassword.error = null
                }
            }
        }

        /**
         * observe the SecureStorage where the auth token is stored in
         */
        SecureStorage.observe(viewLifecycleOwner, Observer {
            if (SecureStorage.getAuthenticationState() == SecureStorage.AuthenticationState.AUTHENTICATED) {

                /**
                 * observe the flat storage to know if the user it part of an flat already
                 * Conditional navigation: if the user is part of a flat then skip the flatFragment otherwise not
                 */
                FlatStorage.observe(viewLifecycleOwner, Observer {
                    if (FlatStorage.getFlatId() == 0) {
                        navController.navigate(R.id.flatFragment)
                    } else {
                        navController.navigate(R.id.navigation_home)
                        MainActivity.navView.visibility = View.VISIBLE
                    }
                })

            }
        })
    }


    /**
     * api call to register an user
     */
    private fun registration() {
        val username = binding.usernameInput.text.toString()
        val mail = binding.mailInput.text.toString()
        val password = binding.passwordInput.text.toString()
        val user = User(null, username, mail, password, System.currentTimeMillis())

        /**
         * checks the api responses and acts accordingly
         * to the stored and managed data in ViewModel
         */
        viewModel.registration(user).observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.loadingPanel.visibility = View.GONE
                }
                Status.ERROR -> {
                    binding.loadingPanel.visibility = View.GONE
                    CustomSnackbar.showSnackbar(
                        binding.root,
                        getString(R.string.error_smt_wrong_html_code, it.code),
                        CustomSnackbar.SnackbarTime.LONG,
                        CustomSnackbar.SnackbarType.ERROR,
                        CustomSnackbar.SnackbarLayoutType.WITHOUT_BottomNavigationView
                    )
                }
                Status.LOADING -> {
                    binding.loadingPanel.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * to validate the user input into the different input filed an to show if necessity error messages
     */
    private fun inputValidation() {
        val mail = binding.mailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()
        val machtingPassword = binding.matchingPasswordInput.text.toString().trim()
        val username = binding.usernameInput.text.toString().trim()

        if (arePasswordMatching(password, machtingPassword)) {
            binding.matchingPassword.error = null
        }
        if (isPasswordValid(password)) {
            binding.password.error = null
        }
        if (isMailValid(mail)) {
            binding.mail.error = null
        }
        if (isUserNameValid(username)) {
            binding.username.error = null
        }
        binding.buttonRegistration.isEnabled =
            isPasswordValid(password) && isMailValid(mail) && isUserNameValid(username) && arePasswordMatching(password, machtingPassword)
    }

    private fun isMailValid(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isUserNameValid(username: String): Boolean {
        return username.length >= 3
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    private fun arePasswordMatching(password_1: String, password_2: String): Boolean {
        return password_1.equals(password_2)
    }
}