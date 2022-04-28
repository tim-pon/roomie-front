package com.example.roomie.presentation.flat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.example.roomie.R
import com.example.roomie.core.Status
import com.example.roomie.databinding.FragmentJoinFlatBinding
import com.example.roomie.presentation.CustomSnackbar
import com.google.zxing.integration.android.IntentIntegrator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class JoinFlatFragment : Fragment() {

    private lateinit var binding: FragmentJoinFlatBinding
    private lateinit var mQrResultLauncher: ActivityResultLauncher<Intent>
    private val viewModel: FlatViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentJoinFlatBinding.inflate(inflater, container, false)

        /**
         * to disable the join flat button
         */
        binding.buttonJoinFlat.isEnabled = false

        /**
         * send entry code from input to api
         */
        binding.buttonJoinFlat.setOnClickListener {
            val entryCode = binding.entryCodeInput.text.toString()
            joinFlat(entryCode)
        }

        /**
         * Starts qr-code scanner
         */
        binding.buttonScanQrcode.setOnClickListener {
            startScanner()
        }

        /**
         * Alternative to "onActivityResult", because that is "deprecated"
         * handling the result from intent
         */
        mQrResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                /**
                 * check if result is available
                 */
                if (it.resultCode == Activity.RESULT_OK) {
                    val result = IntentIntegrator.parseActivityResult(it.resultCode, it.data)
                    if (result.contents != null) {
                        joinFlat(result.contents)
                    }
                }
            }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**
         * checks if the input of the two textinput fields are valid or not
         * if so then enable the button Login so that the user can log in
         */
        binding.entryCodeInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!isEntrycodeValid(s.toString())) {
                    binding.entryCode.error = getString(R.string.error_entry_code)
                    binding.buttonJoinFlat.isEnabled = false
                } else {
                    binding.entryCode.error = ""
                    binding.buttonJoinFlat.isEnabled = true
                }
            }
        })
    }

    /**
     * api call to join flat and get user feedback
     */
    private fun joinFlat(entryCode: String) {
        /**
         * checks the api responses and acts accordingly
         * to the stored and managed data in ViewModel
         */
        viewModel.joinFlat(entryCode).observe(viewLifecycleOwner) {
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

    private fun isEntrycodeValid(entryCode: String): Boolean {
        return (entryCode.length <= 20 && entryCode.length > 19)
    }

    /**
     * Start the QR Scanner
     * Explicit intents specify which applicationExplicit intents specify which application
     */
    private fun startScanner() {
        val scanner = IntentIntegrator(this.activity)
        /**
         * define at which format the scanner should react
         */
        scanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        /**
         * Set Text Prompt at Bottom of QR code Scanner Activity
         */
        scanner.setPrompt("QR Code Scanner")
        mQrResultLauncher.launch(scanner.createScanIntent())
    }

}