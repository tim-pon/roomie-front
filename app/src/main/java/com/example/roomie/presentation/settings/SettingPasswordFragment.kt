package com.example.roomie.presentation.settings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.roomie.R
import com.example.roomie.core.Status
import com.example.roomie.databinding.FragmentSettingPasswordBinding
import com.example.roomie.domain.model.ChangePassword
import com.example.roomie.presentation.CustomSnackbar
import com.example.roomie.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingPasswordFragment : Fragment() {

    private lateinit var binding: FragmentSettingPasswordBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSettingPasswordBinding.inflate(inflater, container, false)

        binding.buttonChangePassword.isEnabled = false
        val editTexts = listOf(binding.newPasswordInput, binding.matchingNewPasswordInput)
        for (editText in editTexts) {
            editText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    var newPassword = binding.newPasswordInput.text.toString().trim()
                    var newPasswordMatching = binding.matchingNewPasswordInput.text.toString().trim()

                    if (!arePasswordMatching(newPassword, newPasswordMatching)) {
                        binding.matchingNewPassword.error = getString(R.string.error_matching_password)
                    } else {
                        binding.matchingNewPassword.error = ""
                    }

                    if (!isPasswordValid(newPassword)) {
                        binding.newPassword.error = getString(R.string.invalid_password)
                    } else {
                        binding.newPassword.error = ""
                    }

                    binding.buttonChangePassword.isEnabled =
                        isPasswordValid(newPassword) && arePasswordMatching(newPassword, newPasswordMatching)
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun afterTextChanged(s: Editable) {}
            })
        }

        binding.buttonChangePassword.setOnClickListener {
            val oldPassword = binding.oldPasswordInput.text.toString()
            val newPassword = binding.newPasswordInput.text.toString()
            val newPasswordMatching = binding.matchingNewPasswordInput.text.toString()
            if (arePasswordMatching(newPassword, newPasswordMatching) && isPasswordValid(newPassword)) {
                val changedPassword = ChangePassword(oldPassword, newPasswordMatching)
                viewModel.changePassword(changedPassword).observe(viewLifecycleOwner) {
                    when (it.status) {
                        Status.SUCCESS -> {
                            view?.findNavController()!!.navigate(SettingPasswordFragmentDirections
                                .actionSettingPasswordToSettingsFragment())
                        }
                        Status.ERROR -> {
                            binding.loadingPanel.visibility = View.GONE
                            CustomSnackbar.showSnackbar(
                                binding.root,
                                getString(R.string.error_smt_wrong_html_code, it.code),
                                CustomSnackbar.SnackbarTime.LONG,
                                CustomSnackbar.SnackbarType.ERROR,
                                CustomSnackbar.SnackbarLayoutType.WITH_BottomNavigationView
                            )
                        }
                        Status.LOADING -> {
                            binding.loadingPanel.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.subpage_change_password_label)
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    private fun arePasswordMatching(password_1: String, password_2: String): Boolean {
        return password_1.equals(password_2)
    }

}