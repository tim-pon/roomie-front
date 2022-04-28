package com.example.roomie.presentation.flat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.roomie.R
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.core.Status
import com.example.roomie.databinding.FragmentCreateFlatBinding
import com.example.roomie.presentation.CustomSnackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateFlatFragment : Fragment() {

    private lateinit var binding: FragmentCreateFlatBinding
    private val viewModel: FlatViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentCreateFlatBinding.inflate(inflater, container, false)

        binding.buttonCreateFlat.isEnabled = false

        binding.buttonCreateFlat.setOnClickListener {
            val name = binding.flatnameInput.text.toString()
            createFlat(name)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.flatnameInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!isFlatNameValid(s.toString())) {
                    binding.flatname.error = getString(R.string.error_flatname)
                    binding.buttonCreateFlat.isEnabled = false
                } else {
                    binding.flatname.error = ""
                    binding.buttonCreateFlat.isEnabled = true
                }
            }
        })
    }

    private fun createFlat(name: String) {
        viewModel.createFlat(name).observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.loadingPanel.visibility = View.GONE
                    val id = it.data?.id
                    if (id != null) {
                        FlatStorage.setFlatId(id)
                    }
                    val savedName = it.data?.name
                    if (savedName != null) {
                        FlatStorage.setFlatName(savedName)
                    }
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

    private fun isFlatNameValid(flatname: String): Boolean {
        return flatname.length > 3
    }

}