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
import com.example.roomie.databinding.FragmentSettingUsernameBinding
import com.example.roomie.presentation.CustomSnackbar
import com.example.roomie.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingUsernameFragment : Fragment() {

    private lateinit var binding: FragmentSettingUsernameBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSettingUsernameBinding.inflate(inflater, container, false)

        binding.buttonChangeUsername.isEnabled = false
        binding.usernameInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!isUserNameValid(s.toString())) {
                    binding.username.error = getString(R.string.error_username)
                    binding.buttonChangeUsername.isEnabled = false
                } else {
                    binding.username.error = ""
                    binding.buttonChangeUsername.isEnabled = true
                }
            }
        })

        binding.buttonChangeUsername.setOnClickListener {
            val newUsername = binding.usernameInput.text.toString()
            if (isUserNameValid(newUsername)) {
                viewModel.changeUsername(newUsername).observe(viewLifecycleOwner) {
                    when (it.status) {
                        Status.SUCCESS -> {
                            view?.findNavController()!!.navigate(SettingUsernameFragmentDirections
                                .actionSettingUsernameToSettingsFragment())
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
                            binding.loadingPanel.visibility = View.GONE
                        }
                    }
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.subpage_change_username_label)
    }

    private fun isUserNameValid(username: String): Boolean {
        return username.length >= 3
    }
}