package com.example.roomie.presentation.finance.statistics

import android.content.Context
import android.provider.Settings.Global.getString
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
import com.example.roomie.databinding.ItemFinanceStatisticsBinding
import com.example.roomie.domain.model.FinanceStatistics

class StatisticsAdapter(
    private val context: Context,
    private val lazyHeader: LazyHeaders
    ): ListAdapter<FinanceStatistics, StatisticsAdapter.StatisticsViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatisticsViewHolder {
        val binding = ItemFinanceStatisticsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return StatisticsViewHolder(binding, context, lazyHeader)
    }

    override fun onBindViewHolder(holder: StatisticsViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null)
            holder.bind(currentItem)
    }


    class StatisticsViewHolder(
        private val binding: ItemFinanceStatisticsBinding,
        private val context: Context,
        private val lazyHeader: LazyHeaders
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(financeStatistics: FinanceStatistics) {
            binding.apply {
                this.financeStatistics = financeStatistics
                if (financeStatistics.balance < 0) {
                    amount.setTextColor(android.graphics.Color.RED)
                } else if (financeStatistics.balance > 0) {
                    amount.setTextColor(android.graphics.Color.GREEN)
                }
                executePendingBindings()
            }

            Glide.with(context)
                .load(GlideUrl(Constants.BASE_URL + "user/image/${financeStatistics.userId}", lazyHeader))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_account)
                .into(binding.icon)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<FinanceStatistics>() {

        override fun areItemsTheSame(oldItem: FinanceStatistics, newItem: FinanceStatistics) =
            oldItem.userId == newItem.userId

        override fun areContentsTheSame(oldItem: FinanceStatistics, newItem: FinanceStatistics) =
            oldItem == newItem
    }
}