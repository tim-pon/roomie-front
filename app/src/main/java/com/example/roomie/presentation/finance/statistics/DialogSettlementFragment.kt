package com.example.roomie.presentation.finance.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.viewModels
import com.bumptech.glide.load.model.LazyHeaders
import com.example.roomie.R
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.databinding.DialogSettleFinanceBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class DialogSettlementFragment: BottomSheetDialogFragment() {

    private lateinit var binding: DialogSettleFinanceBinding
    private val viewModel: StatisticsViewModel by viewModels()
    @Inject lateinit var lazyHeaders: LazyHeaders
    private lateinit var settlementAdapter: SettlementAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogSettleFinanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.listSettlements.adapter = initAdapter()

        val btnCancel = binding.root.findViewById<ImageButton>(R.id.btn_cancel)

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun initAdapter(): SettlementAdapter {
        val flatId = FlatStorage.getFlatId()
        settlementAdapter = SettlementAdapter(lazyHeaders, requireContext())
        viewModel.settleFinance(flatId).observe(viewLifecycleOwner) {
            settlementAdapter.submitList(it.data)

            requireActivity().supportFragmentManager.setFragmentResult("settled", Bundle.EMPTY)
        }
        return settlementAdapter
    }
}