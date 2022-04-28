package com.example.roomie.presentation.finance.statistics

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.bumptech.glide.load.model.LazyHeaders
import com.example.roomie.R
import com.example.roomie.core.Status
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.databinding.FragmentFinanceStatisticsBinding
import com.example.roomie.presentation.CustomSnackbar
import com.example.roomie.presentation.finance.FinanceFragmentDirections
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    private lateinit var binding : FragmentFinanceStatisticsBinding
    private lateinit var statisticsAdapter: StatisticsAdapter
    private val viewModel: StatisticsViewModel by viewModels()
    @Inject lateinit var lazyHeader: LazyHeaders

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFinanceStatisticsBinding.inflate(inflater, container, false)

        binding.settleTransactions.setOnClickListener {
            /**
             * create alert dialog to ensure the user will really settle finances
             */
            MaterialAlertDialogBuilder(this.requireContext())
                .setTitle(getString(R.string.dialog_settle_finances_title))
                .setMessage(getString(R.string.dialog_settle_finance_message))
                .setPositiveButton(getString(R.string.settle_yes)
                ) { dialog, whichButton ->
                    val direction = FinanceFragmentDirections.actionStatisticsFragmentToSettlementDialog()
                    it.findNavController().navigate(direction)
                }
                .setNegativeButton(getString(R.string.settle_no), null).show()
        }

        requireActivity().supportFragmentManager.setFragmentResultListener("settled", this) { _, _ ->
            binding.recyclerViewStatistics.adapter = initAdapter()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewmodel = this@StatisticsFragment.viewModel
            recyclerViewStatistics.adapter = initAdapter()
        }
    }

    /**
     * set adapter again because caching is not includes
     * so to get the newest data available we have to set the adapter again with the newest data
     * statisticsAdapter.notifyDataSetChanged() won't work
     */
    override fun onResume() {
        super.onResume()
        binding.recyclerViewStatistics.adapter = initAdapter()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initAdapter(): StatisticsAdapter {
        val flatId = FlatStorage.getFlatId()

        statisticsAdapter = StatisticsAdapter(this.requireContext(), lazyHeader)

        viewModel.getFinanceStatistics(flatId).observe(viewLifecycleOwner) { stats ->
            statisticsAdapter.submitList(stats.data?.sortedBy { it.balance })

            stats.data?.any { it.balance != 0.0 }?.let { viewModel.isEnabled?.set(it) }
            // Response handling
            if (stats.status == Status.ERROR)
                CustomSnackbar.defaultError(requireView(), stats.code)
        }
        return statisticsAdapter
    }
}