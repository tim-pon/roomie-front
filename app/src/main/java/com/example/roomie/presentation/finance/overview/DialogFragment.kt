package com.example.roomie.presentation.finance.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.viewModels
import com.bumptech.glide.load.model.LazyHeaders
import com.example.roomie.R
import com.example.roomie.core.Status
import com.example.roomie.databinding.FragmentAddTransactionBinding
import com.example.roomie.presentation.CustomSnackbar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentAddTransactionBinding
    private val viewModel: DialogViewModel by viewModels()
    @Inject
    lateinit var lazyHeaders: LazyHeaders
    private lateinit var userAdapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@DialogFragment.viewModel
            listUsers.adapter = initAdapter()
        }
        initCheckboxes()

        val btnCancel = binding.root.findViewById<ImageButton>(R.id.btn_cancel)
        val btnSave = binding.root.findViewById<MaterialButton>(R.id.btn_save)

        btnCancel.setOnClickListener { dismiss() }
        btnSave.setOnClickListener { saveArticle() }
    }

    /**
     * Subscribes users livedata to ui
     * @return adapter with list of users
     */
    private fun initAdapter(): UserAdapter {
        userAdapter = UserAdapter(lazyHeaders, requireContext())
        viewModel.flatUser.observe(viewLifecycleOwner) {
            userAdapter.submitList(it.data?.users)
        }
        return userAdapter
    }

    /**
     * Sets property isInTranscation of user objects in flatUsers.
     * Only executes if both livedata are loaded successful
     */
    private fun initCheckboxes() {
        viewModel.dependentLiveData.observe(viewLifecycleOwner) {
            if (viewModel.transaction.value?.status == Status.SUCCESS
                && viewModel.flatUser.value?.status == Status.SUCCESS
            ) {
                // for each user in transaction find the same user in flatUser
                // and set isInTransaction to true
                viewModel.transaction.value?.data?.users?.map { transactionUser ->
                    viewModel.flatUser.value?.data?.users?.find { flatUser ->
                        transactionUser.id == flatUser.id
                    }?.isInTransaction = true
                }
            }
        }
    }

    private fun saveArticle() {
        viewModel.transaction.value?.data?.users =
            viewModel.flatUser.value?.data?.users?.filter { it.isInTransaction }!!

        // Response handling
        viewModel.updateTransaction().observe(viewLifecycleOwner) {
            if (it.status !== Status.LOADING)
                dismiss()
            if (it.status == Status.ERROR)
                CustomSnackbar.defaultError(requireView(), it.code)
        }

    }
}