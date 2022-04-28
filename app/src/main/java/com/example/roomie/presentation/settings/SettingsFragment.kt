package com.example.roomie.presentation.settings

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.roomie.R
import com.example.roomie.core.Constants
import com.example.roomie.core.Status
import com.example.roomie.databinding.FragmentSettingsBinding
import com.example.roomie.presentation.CustomSnackbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.FileDescriptor
import java.io.IOException
import javax.inject.Inject


@AndroidEntryPoint
class SettingsFragment() : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var settingsAdapter: SettingsAdapter
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var bottomSheetDialog: BottomSheetDialog
    @Inject lateinit var lazyHeader: LazyHeaders

    private lateinit var image_uri: Uri
    private val IMAGE_CAPTURE_CODE = 654
    private val RESULT_LOAD_IMAGE = 123

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        /**
         * static data for recyclerView
         */
        val heading = resources.getStringArray(R.array.settings_headline)
        val description = resources.getStringArray(R.array.settings_description)

        /**
         * creating adapter to fill recyclerview with data
         */
        val recyclerView = binding.settingsRecyclerView
        settingsAdapter = SettingsAdapter(heading, description)
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = settingsAdapter

        /**
         * add item decoration for better visual understanding
         */
        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        divider.dividerInsetStart = 120
        divider.dividerInsetEnd = 20
        recyclerView.addItemDecoration(divider)

        binding.icon.setOnClickListener {
            /**
             * checks if Permission is given to access camera
             * if not then ask for permission
             */
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            ) {
                createListDialog()
            } else {
                val permission: Array<String> = arrayOf(Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permission, 112)

            }
        }

        binding.buttonLogout.setOnClickListener {

            /**
             * create alert dialog to ensure the user will really logout
             */
            MaterialAlertDialogBuilder(this.requireContext())
                .setTitle(getString(R.string.logout_alert_dialog))
                .setMessage(getString(R.string.logout_check))
                .setPositiveButton(getString(R.string.logout_yes)
                ) { dialog, whichButton ->
                    // clear shared pref and backstack an gets the user back to the landing page
                    // FlatStorage.setFlatId(0)
                    // SecureStorage.setAuthToken("", SecureStorage.AuthenticationState.UNAUTHENTICATED)
                    // findNavController().popBackStack(R.id.loginFragment, true);
                    // findNavController().navigate(R.id.loginFragment)
                    // deletes the complete data of the app (shared pref, database, chase ...)
                    deleteAppData(this.requireContext())
                }
                .setNegativeButton(getString(R.string.logout_no), null).show()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.getUser().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.loadingPanel.visibility = View.VISIBLE
                }
                Status.SUCCESS -> {
                    binding.loadingPanel.visibility = View.GONE
                    binding.email.text = it.data?.email
                    binding.username.text = it.data?.username
                    getUserImage(binding.icon, this.requireContext())
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
            }
        }
    }


    /**
     * after requestPermissions we check if the permission is given or not
     * if it is given then show the dialog if not nothing happens
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            112 -> {
                // If request is cancelled, the result arrays are empty.
                if (permissions.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    createListDialog()
                }
                return
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /**
         * check if image is available
         */
        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
            val bitmap = uriToBitmap(image_uri)
            uploadImage(bitmap!!)
        }
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            image_uri = data.data!!
            val bitmap = uriToBitmap(image_uri)
            uploadImage(bitmap!!)
        }

    }

    /**
     * to upload image to api
     */
    private fun uploadImage(bitmapImage: Bitmap) {
        val stream = ByteArrayOutputStream()
        /**
         * decode bitmap to JPEG
         */
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 20, stream)
        val byteArray = stream.toByteArray()
        val requestFile: RequestBody =
            RequestBody.create("text/plain".toMediaTypeOrNull(), byteArray)
        val fileToUpload: MultipartBody.Part =
            MultipartBody.Part.createFormData("image", "image", requestFile)
        viewModel.uploadImage(fileToUpload).observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.loadingPanel.visibility = View.VISIBLE
                }
                Status.SUCCESS -> {
                    binding.icon.setImageBitmap(bitmapImage)
                    binding.loadingPanel.visibility = View.GONE
                    bottomSheetDialog.dismiss()
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
     * delete hole storage of the app
     * and kills the background as a foreground service
     */
    private fun deleteAppData(context: Context) {
        try {
            // clearing app data
            val packageName = context.packageName
            val runtime = Runtime.getRuntime()
            runtime.exec("pm clear $packageName")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * display dialog so that the user can choose between camera and gallery to get an image
     */
    private fun createListDialog() {
        bottomSheetDialog = BottomSheetDialog(this.requireContext())
        bottomSheetDialog.setContentView(R.layout.dialog_chose_photo_app)
        val cancel = bottomSheetDialog.findViewById<ImageButton>(R.id.btn_cancel)!!
        val camera = bottomSheetDialog.findViewById<Button>(R.id.button_photo)!!
        val gallery = bottomSheetDialog.findViewById<Button>(R.id.button_gallery)!!
        cancel.setOnClickListener { bottomSheetDialog.dismiss() }
        camera.setOnClickListener { openCamera() }
        gallery.setOnClickListener { openGallery() }
        bottomSheetDialog.show()
    }

    /**
     * get image from gallery/storage
     */
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE)
    }

    /**
     * get image from camera
     */
    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, getString(R.string.new_picture))
        values.put(MediaStore.Images.Media.DESCRIPTION, getString(R.string.from_camera))
        image_uri =
            context?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }


    /**
     * get user image via glide
     * By default Glide uses a custom HttpUrlConnection so it does not require any okhttp elements
     */
    private fun getUserImage(view: CircleImageView, context: Context) {
        val url = Constants.BASE_URL + "user/image/"
        Glide.with(context)
            .load(GlideUrl(url, lazyHeader))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.ic_account)
            .into(view)
    }

    /**
     * convert uri (Uniform Resource Identifier) to bitmap
     */
    private fun uriToBitmap(uri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor: ParcelFileDescriptor? =
                context?.contentResolver?.openFileDescriptor(uri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

}