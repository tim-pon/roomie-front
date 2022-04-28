package com.example.roomie.presentation.finance.overview

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.roomie.R
import com.example.roomie.core.Constants
import com.example.roomie.databinding.ItemTransactionBinding
import com.example.roomie.domain.model.TransactionWithCreatorAndUsers
import com.example.roomie.domain.model.User
import com.example.roomie.presentation.finance.FinanceFragmentDirections

class TransactionAdapter (
    private val context: Context,
    private val lazyHeader: LazyHeaders
): ListAdapter<TransactionWithCreatorAndUsers, TransactionAdapter.TransactionViewHolder>(
    TransactionAdapter) {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransactionViewHolder(binding, lazyHeader, context)
    }

    override fun onBindViewHolder(
        holder: TransactionViewHolder,
        position: Int
    ) {
        val currentItem = getItem(position)
        if (currentItem != null)
            holder.bind(currentItem)
    }

    class TransactionViewHolder(
        private val binding: ItemTransactionBinding,
        private val lazyHeader: LazyHeaders,
        private val context: Context
    ): RecyclerView.ViewHolder(binding.root) {

        init {
            binding.setClickListener { view ->
                val trans = binding.transactionWithCreatorAndUsers?.transactionWithCreator?.transaction!!
                navigateToTransactionDialog(trans.flatId!!, trans.id!!, view)
            }
        }

        private fun navigateToTransactionDialog(
            flatId: Int,
            transactionId: Int,
            view: View
        ) {
            val direction = FinanceFragmentDirections
                .actionFinanceOverviewFragmentToTransactionDialog(flatId,  transactionId)
            view.findNavController().navigate(direction)
        }

        fun bind(transactionWithCreatorAndUsers: TransactionWithCreatorAndUsers) {
            binding.apply {
                this.transactionWithCreatorAndUsers = transactionWithCreatorAndUsers
                executePendingBindings()
            }

            setUserImage(
                transactionWithCreatorAndUsers.transactionWithCreator.creator, binding.imgCreator)

        }

        private fun setUserImage(user: User?, view: ImageView) =
            Glide.with(context)
                .load(GlideUrl(user?.getImageUrl()?: Constants.BASE_URL + "user/image", lazyHeader))
                .placeholder(R.drawable.ic_user)
                .into(view)

    }

    companion object DiffCallback : DiffUtil.ItemCallback<TransactionWithCreatorAndUsers>() {

        override fun areItemsTheSame(oldItem: TransactionWithCreatorAndUsers, newItem: TransactionWithCreatorAndUsers) =
            oldItem.transactionWithCreator.transaction.id == newItem.transactionWithCreator.transaction.id

        override fun areContentsTheSame(oldItem: TransactionWithCreatorAndUsers, newItem: TransactionWithCreatorAndUsers) =
            oldItem == newItem

    }
}