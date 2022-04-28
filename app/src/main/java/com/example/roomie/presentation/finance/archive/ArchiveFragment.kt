package com.example.roomie.presentation.finance.archive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.load.model.LazyHeaders
import com.example.roomie.core.Status
import com.example.roomie.databinding.FragmentFinanceArchiveBinding
import com.example.roomie.presentation.CustomSnackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ArchiveFragment : Fragment() {

    private lateinit var binding : FragmentFinanceArchiveBinding
    private val viewModel: ArchiveViewModel by viewModels()
    lateinit var archiveAdapter: ArchiveAdapter
    @Inject lateinit var lazyHeaders: LazyHeaders


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFinanceArchiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@ArchiveFragment.viewModel
            recyclerViewFinanceArchive.adapter = initAdapter()
            swipeRefreshFinanceArchive.setOnRefreshListener {
                this@ArchiveFragment.viewModel.refresh(true)
            }
        }
    }

    /**
     * Subscribes paid transactions livedata to ui
     * @return adapter with list of transactions by descending time
     */
    private fun initAdapter(): ArchiveAdapter {
        archiveAdapter = ArchiveAdapter(requireContext(), lazyHeaders)

        viewModel.transactionsWithCreatorAndUsers.observe(viewLifecycleOwner) { transactions ->
            archiveAdapter.submitList(transactions.data?.filter { it.transactionWithCreator.transaction.paid == true }
                ?.sortedByDescending {
                    it.transactionWithCreator.transaction.createdAt })

            binding.swipeRefreshFinanceArchive.isRefreshing = transactions.status == Status.LOADING

            if(transactions.status == Status.ERROR)
                CustomSnackbar.defaultError(requireView(), transactions.code)
        }
        return archiveAdapter
    }
}