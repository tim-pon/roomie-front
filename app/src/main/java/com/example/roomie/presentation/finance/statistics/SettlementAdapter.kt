package com.example.roomie.presentation.finance.statistics

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.roomie.R
import com.example.roomie.core.Constants
import com.example.roomie.databinding.ItemSettleBinding
import com.example.roomie.domain.model.FinanceSettle

class SettlementAdapter (
    private val lazyHeader: LazyHeaders,
    private val context: Context,
): ListAdapter<FinanceSettle, SettlementAdapter.SettlementViewHolder>(SettlementAdapter) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SettlementViewHolder {
        val binding = ItemSettleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SettlementViewHolder(binding, context, lazyHeader)
    }

    override fun onBindViewHolder(
        holder: SettlementViewHolder,
        position: Int,
    ) {
        val currentItem = getItem(position)
        if (currentItem != null)
            holder.bind(currentItem)
    }


    class SettlementViewHolder(
        private val binding: ItemSettleBinding,
        private val context: Context,
        private val lazyHeader: LazyHeaders
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(settlement: FinanceSettle) {
            binding.apply {
                this.financeSettle = settlement
                executePendingBindings()
            }

            Glide.with(context)
                .load(GlideUrl(Constants.BASE_URL + "user/image/${settlement.receiverId}", lazyHeader))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_account)
                .into(binding.imgUserReceiver)

            Glide.with(context)
                .load(GlideUrl(Constants.BASE_URL + "user/image/${settlement.giverId}", lazyHeader))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_account)
                .into(binding.imgUserGiver)

            binding.arrow.setImageResource(R.drawable.ic_arrow_line)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<FinanceSettle>() {

        override fun areItemsTheSame(oldItem: FinanceSettle, newItem: FinanceSettle) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: FinanceSettle, newItem: FinanceSettle) =
            oldItem == newItem

    }
}