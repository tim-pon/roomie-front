package com.example.roomie.presentation.authentication

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.roomie.R
import com.example.roomie.core.Status
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.core.sharedpreferences.SecureStorage
import com.example.roomie.databinding.FragmentLoginBinding
import com.example.roomie.domain.model.User
import com.example.roomie.presentation.CustomSnackbar
import com.example.roomie.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    /**
     * @param binding to more easily write code that interacts with view
     * @param navController manages app navigation from one fragment to another
     * @param viewModel to store and manage UI-related data in a lifecycle conscious way for authentication
     */
    private lateinit var binding: FragmentLoginBinding
    private lateinit var navController: NavController
    private val viewModel: AuthenticationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        navController = findNavController()

        /**
         * to make the navigation bar disappeared in the layout
         */
        MainActivity.navView.visibility = View.GONE

        /**
         * to disable the login button
         */
        binding.buttonLogin.isEnabled = false

        /**
         * to get to the registry by button click
         */
        binding.buttonRegistration.setOnClickListener { navController.navigate(R.id.action_loginFragment_to_registrationFragment) }

        /**
         * button to perform the login action
         */
        binding.buttonLogin.setOnClickListener { login() }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.login_title)

        /**
         * to observe multiplie textinput fields at the same time
         */
        val editTexts = listOf(binding.mailInput, binding.passwordInput)

        /**
         * checks if the input of the two textinput fields are valid or not
         * if so then enable the button Login so that the user can log in
         */
        for (editText in editTexts) {
            editText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    /**
                     * removes withspaces
                     */
                    val mail = binding.mailInput.text.toString().trim()
                    val password = binding.passwordInput.text.toString().trim()

                    binding.buttonLogin.isEnabled =
                        isUserNameValid(mail) && isPasswordValid(password)

                    /**
                     * clears error as soon as input is valid
                     */
                    if (isUserNameValid(mail)) {
                        binding.mail.error = null
                    }
                    /**
                     * clears error as soon as input is valid
                     */
                    if (isPasswordValid(password)) {
                        binding.password.error = null
                    }
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

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
        binding.passwordInput.setOnFocusChangeListener { v, hasFocus ->
            val password = binding.passwordInput.text.toString()
            if (!hasFocus) {
                if (!isPasswordValid(password)) {
                    binding.password.error = getString(R.string.invalid_password)
                } else {
                    binding.mail.error = null
                }
            } else {
                if (isPasswordValid(password)) {
                    binding.mail.error = null
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
     * api call to login and get user feedback
     */
    private fun login() {
        val mail = binding.mailInput.text.toString()
        val password = binding.passwordInput.text.toString()
        val loginAttempt = User(null, null, mail, password, System.currentTimeMillis())
        /**
         * checks the api responses and acts accordingly
         * to the stored and managed data in ViewModel
         */
        viewModel.login(loginAttempt).observe(viewLifecycleOwner) {
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
     * to check if the entered mail is valid or not
     *
     * @param email the string from the inputtext
     */
    private fun isUserNameValid(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * to check if the entered password is valid or not
     *
     * @param password the string from the inputtext
     */
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

}
