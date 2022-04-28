package com.example.roomie.presentation.finance.archive

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.roomie.R
import com.example.roomie.core.Constants
import com.example.roomie.databinding.ItemTransactionBinding
import com.example.roomie.domain.model.TransactionWithCreatorAndUsers
import com.example.roomie.domain.model.User

class ArchiveAdapter(
    private val context: Context,
    private val lazyHeader: LazyHeaders
) : ListAdapter<TransactionWithCreatorAndUsers, ArchiveAdapter.TransactionViewHolder>(
    ArchiveAdapter) {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransactionViewHolder(binding, lazyHeader, context)
    }

    override fun onBindViewHolder(
        holder: TransactionViewHolder,
        position: Int,
    ) {
        val currentItem = getItem(position)
        if (currentItem != null)
            holder.bind(currentItem)
    }

    class TransactionViewHolder(
        private val binding: ItemTransactionBinding,
        private val lazyHeader: LazyHeaders,
        private val context: Context,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transactionWithCreatorAndUsers: TransactionWithCreatorAndUsers) {
            binding.apply {
                this.transactionWithCreatorAndUsers = transactionWithCreatorAndUsers
                executePendingBindings()
            }

            setUserImage(
                transactionWithCreatorAndUsers.transactionWithCreator.creator, binding.imgCreator)

        }

        /**
         * set userImage directly into item view with Glide
         * caching is disabled
         * if no picture can be obtained, a placeholder image is set
         */
        private fun setUserImage(user: User?, view: ImageView) =
            Glide.with(context)
                .load(GlideUrl(Constants.BASE_URL + "user/image/${user?.id}",
                    lazyHeader))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_account)
                .into(view)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TransactionWithCreatorAndUsers>() {

        override fun areItemsTheSame(
            oldItem: TransactionWithCreatorAndUsers,
            newItem: TransactionWithCreatorAndUsers,
        ) =
            oldItem.transactionWithCreator.transaction.id == newItem.transactionWithCreator.transaction.id

        override fun areContentsTheSame(
            oldItem: TransactionWithCreatorAndUsers,
            newItem: TransactionWithCreatorAndUsers,
        ) =
            oldItem == newItem

    }
}