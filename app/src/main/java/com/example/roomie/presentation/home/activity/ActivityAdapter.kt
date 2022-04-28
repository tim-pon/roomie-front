package com.example.roomie.presentation.home.activity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.roomie.databinding.ItemActivityBinding
import com.example.roomie.domain.model.Activity
import com.example.roomie.presentation.home.HomeFragmentDirections

class ActivityAdapter(
    val context: Context,
) : ListAdapter<Activity, ActivityAdapter.ActivityViewHolder>(
    DiffCallback
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = ItemActivityBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ActivityViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null)
            holder.bind(currentItem)
    }

    class ActivityViewHolder(
        private val binding: ItemActivityBinding,
        private val context: Context,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.defaultActivityButton.setOnClickListener { view ->
                binding.activity?.let { activity ->
                    navigateToActivityCategory(activity.activity, view)
                }
            }
        }

        fun bind(activity: Activity) {
            binding.apply {
                this.labelActivityItemDescription.text = ActivityMessages.getActivityItemDescription(activity, context)
                this.imgActivityCategory.setImageDrawable(ActivityMessages.getCategoryItem(activity, context))
                this.activityCreatedOn.text = ActivityMessages.getTimeForSystemTimeZone(activity, context)
                this.activity = activity
                executePendingBindings()
            }
        }

        /**
         * clicking on a finance or shopping activity brings you to the main page of either finance
         * or shopping
         */
        private fun navigateToActivityCategory(
            activity: String,
            view: View,
        ) {
            val direction = when (activity) {
                "ADD_LIST", "ADD_ARTICLE" -> HomeFragmentDirections
                    .actionActivityToShoppingFragment()
                "ADD_TRANSACTION", "PAY_TRANSACTIONS" -> HomeFragmentDirections.actionActivityToFinanceFragment()
                else -> null
            }
            if (direction != null)
                view.findNavController().navigate(direction)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Activity>() {

        override fun areItemsTheSame(oldItem: Activity, newItem: Activity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Activity, newItem: Activity) =
            oldItem == newItem

    }

}