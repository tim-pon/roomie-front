package com.example.roomie.presentation.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.roomie.R
import com.example.roomie.core.Status
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.databinding.FragmentSettingFlatInfoBinding
import com.example.roomie.domain.model.Flat
import com.example.roomie.presentation.CustomSnackbar
import com.example.roomie.presentation.MainActivity
import com.example.roomie.presentation.flat.FlatViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SettingFlatInfoFragment : Fragment() {

    private lateinit var binding: FragmentSettingFlatInfoBinding
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val flatViewModel: FlatViewModel by viewModels()
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSettingFlatInfoBinding.inflate(inflater, container, false)
        navController = findNavController()

        /**
         * option to leave one flat
         */
        binding.buttonLeaveFlat.setOnClickListener {
            getUserFlats()
        }

        /**
         * option to copy the entry code per copy icon
         */
        binding.entryCode.setEndIconOnClickListener {
            val entryCode = binding.entryCodeInput.text.toString()
            copyToClipboard(entryCode)
            Toast.makeText(this.requireContext(), entryCode, Toast.LENGTH_LONG).show()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.subpage_flat_info_label)

        val flatId = FlatStorage.getFlatId()
        /**
         * to get all infos of the current flat to display these infos
         */
        settingsViewModel.getFlatInfo(flatId!!).observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.loadingPanel.visibility = View.GONE
                    val entryCode = it.data?.entryCode ?: ""
                    binding.flatnameInput.setText(it.data?.name)
                    binding.entryCodeInput.setText(entryCode)
                    /**
                     * create qr-code and display it in the image
                     */
                    binding.qrCode.setImageBitmap(getQrCodeBitmap(entryCode))
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

    /**
     * to get all flats from one user
     */
    private fun getUserFlats() {
        settingsViewModel.getFlatsFromUser().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.loadingPanel.visibility = View.VISIBLE
                }
                Status.SUCCESS -> {
                    binding.loadingPanel.visibility = View.GONE
                    val list: List<Flat> = it.data!!
                    flatOptions(list)
                }
                Status.ERROR -> {
                    binding.loadingPanel.visibility = View.GONE
                    CustomSnackbar.showSnackbar(
                        binding.root,
                        it.code.toString(),
                        CustomSnackbar.SnackbarTime.LONG,
                        CustomSnackbar.SnackbarType.ERROR,
                        CustomSnackbar.SnackbarLayoutType.WITH_BottomNavigationView
                    )
                }
            }
        }
    }

    /**
     * to copy text into the clipboard
     * @param entryCode the string to copy (in our case the entry code to join one flat)
     */
    private fun copyToClipboard(entryCode: String) {
        val clipBoard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip : ClipData = ClipData.newPlainText("simple Text", entryCode)
        clipBoard.setPrimaryClip(clip)
    }

    /**
     * leave flat an then choose one option
     * @param flats list of all flat in which the user is part of
     */
    private fun flatOptions(flats: List<Flat>) {

        /**
         * get name and id out of flat
         */
        val flatNameList: ArrayList<String> = ArrayList()
        val flatIdList: ArrayList<Int> = ArrayList()
        val currentFlatId = FlatStorage.getFlatId()

        /**
         * add all flats except the current one in which the user is part of
         * if the user is only in one flat (in the current one) then the list will be empty
         */
        flats.forEach { flat: Flat ->
            if (currentFlatId != flat.id!!) {
                flatNameList.add(flat.name!!)
                flatIdList.add(flat.id)
            }
        }
        val flatnames = flatNameList.toTypedArray()

        /**
         * create Alert dialog with users flat so he can join/create a new flat or swap to another one of his own ones
         */
        MaterialAlertDialogBuilder(this.requireContext())
            .setTitle(getString(R.string.leave_flat_title))

            /**
             * this function is not in use yet because one user can not be in multiple flats at the same time (ui is not available but backend logic can handle it)
             */
            .setItems(flatnames) { dialog, item -> // Do something with the selection
                when (item) {
                    /**
                     * option to switch in one of his other flats
                     */
                    item -> {
                        leaveFlat(currentFlatId)
                        /**
                         * set new flatid into the storage so conditional navigation works an the user get directly to the home screen
                         */
                        FlatStorage.setFlatId(flatIdList[item])
                        /**
                         * for user feedback
                         */
                        CustomSnackbar.showSnackbar(binding.root,
                            getString(R.string.snackbar_joined_flat) + " ${flatNameList[item]}",
                            CustomSnackbar.SnackbarTime.INFINIE,
                            CustomSnackbar.SnackbarType.SUCCESS,
                            CustomSnackbar.SnackbarLayoutType.WITH_BottomNavigationView
                        )
                        navController.popBackStack(R.id.loginFragment, true);
                        navController.navigate(R.id.navigation_home)
                    }
                    else -> {
                        CustomSnackbar.showSnackbar(binding.root,
                            getString(R.string.snackbar_error_message),
                            CustomSnackbar.SnackbarTime.INFINIE,
                            CustomSnackbar.SnackbarType.ERROR,
                            CustomSnackbar.SnackbarLayoutType.WITH_BottomNavigationView
                        )
                    }
                }
            }
            .setPositiveButton(getString(R.string.leave_flat_yes)
            ) { dialog, whichButton ->
                /**
                 * option to creat/join a new flat
                 */
                navController.navigate(R.id.flatFragment)
                MainActivity.navView.visibility = View.GONE
                leaveFlat(currentFlatId)
                /**
                 * removes the back Stack till it reaches the first fragment so it's removes all the back stack completely
                 * so the user is not able to get into his old flat
                 */
                navController.popBackStack(R.id.loginFragment, true);
                navController.navigate(R.id.flatFragment)
                FlatStorage.setFlatId(0)
                MainActivity.navView.visibility = View.GONE
            }
            .setMessage(getString(R.string.leave_flat_check))
            .setNegativeButton(getString(R.string.leave_flat_no), null)
            .show()
    }

    /**
     * request to leave the current flat
     */
    private fun leaveFlat(flatId: Int) {
        flatViewModel.leaveFlat(flatId).observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.loadingPanel.visibility = View.VISIBLE
                }
                Status.SUCCESS -> {
                    binding.loadingPanel.visibility = View.GONE
                }
                Status.ERROR -> {
                    binding.loadingPanel.visibility = View.GONE
                    CustomSnackbar.showSnackbar(
                        binding.root,
                        it.code.toString(),
                        CustomSnackbar.SnackbarTime.LONG,
                        CustomSnackbar.SnackbarType.ERROR,
                        CustomSnackbar.SnackbarLayoutType.WITH_BottomNavigationView
                    )
                }
            }
        }
    }

    /**
     * to generate an qr-code from string
     * @param entryCode
     * @return qr-code as bitmap image
     */
    private fun getQrCodeBitmap(entryCode: String): Bitmap {
        val size = 512 //pixels
        val hints = hashMapOf<EncodeHintType, Int>().also {
            it[EncodeHintType.MARGIN] = 1
        } // Make the QR code buffer border narrower
        val bits = QRCodeWriter().encode(entryCode, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }

}