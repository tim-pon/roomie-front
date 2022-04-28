package com.example.roomie.presentation.finance.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.roomie.core.Status
import com.example.roomie.databinding.FragmentFinanceOverviewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.bumptech.glide.load.model.LazyHeaders
import com.example.roomie.R
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.domain.model.*
import com.example.roomie.presentation.CustomSnackbar
import com.example.roomie.presentation.SwipeToDelete
import com.example.roomie.presentation.finance.FinanceFragmentDirections
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@AndroidEntryPoint
class OverviewFragment : Fragment() {

    private lateinit var binding : FragmentFinanceOverviewBinding
    private val viewModel: TransactionsViewModel by viewModels()
    lateinit var adapter: TransactionAdapter
    @Inject lateinit var lazyHeaders: LazyHeaders
    private lateinit var userAdapter: UserAdapter
    lateinit var list: List<User>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFinanceOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.apply {

            lifecycleOwner = viewLifecycleOwner
            viewModel = this@OverviewFragment.viewModel

            recyclerViewFinanceTransactions.adapter = initAdapter()
            swiperefresh.setOnRefreshListener { this@OverviewFragment.viewModel.refresh(true) }

            // Navigates to TransactionDialogFragment
            btnAddTransaction.setOnClickListener { createTransaction(it) }
        }

        userAdapter = UserAdapter(lazyHeaders, requireContext())

        val itemTouchHelper = ItemTouchHelper(SwipeToDelete(requireContext(), ::deleteTransaction))
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewFinanceTransactions)

    }

    /**
     * Subscribes transactions livedata to ui
     * @return adapter with list of transactions
     */
    private fun initAdapter(): TransactionAdapter {
        adapter = TransactionAdapter(requireContext(),lazyHeaders)

        viewModel.transactionsWithCreatorAndUsers.observe(viewLifecycleOwner) { transaction ->

            binding.isEmpty = transaction.data?.filter {
                it.transactionWithCreator.transaction.paid == false }.isNullOrEmpty()
                && transaction.status !== Status.LOADING

            binding.btnEmpty.setOnClickListener { createTransaction(it) }
            adapter.submitList(transaction.data?.filter { it.transactionWithCreator.transaction.paid == false }
                ?.sortedByDescending { it.transactionWithCreator.transaction.getCreatedAtDate() })

            // Response handling
            binding.swiperefresh.isRefreshing = transaction.status == Status.LOADING
            if (transaction.status == Status.ERROR)
                CustomSnackbar.defaultError(requireView(), transaction.code)
        }
        return adapter
    }


    /**
     * Deletes transaction for swiped item
     * Callback function for ItemTouchHelper onSwiped function
     *
     * @param adapterPosition position of the transaction to delete
     */
    private fun deleteTransaction(adapterPosition: Int) {

        /**
         * Executes the deletion and handles response status.
         * Inner function to unclutter dialog builder. Called by onPositiveButton of Dialog
         *
         * @param transaction will be deleted
         */
        fun delete(transaction: TransactionWithCreatorAndUsers) {
            viewModel.deleteTransaction(transaction).observe(viewLifecycleOwner) {
                if (it.status == Status.ERROR)
                    CustomSnackbar.defaultError(requireView(),it.code)
            }
        }

        // get transaction from adapter position
        val transaction = adapter.currentList[adapterPosition]

        // build delete dialog
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_delete_title,
                getString(R.string.dialog_delete_transaction_title)))
            .setMessage(R.string.dialog_delete_transaction_message)
            .setPositiveButton(R.string.dialog_delete_yes) { _, _ -> delete(transaction) }
            .setNegativeButton(R.string.dialog_delete_no) { _, _ ->  /** handled by onDismiss*/ }
            .setOnDismissListener { adapter.notifyItemChanged(adapterPosition) }
            .show()
    }

    private fun createTransaction(view: View) {
        val direction = FinanceFragmentDirections
            .actionFinanceOverviewFragmentToTransactionDialog(FlatStorage.getFlatId()!!)
        view.findNavController().navigate(direction)
    }
}