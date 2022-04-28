package com.example.roomie.presentation.finance.overview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.roomie.databinding.ItemUserBinding
import com.example.roomie.domain.model.User

class UserAdapter (
    private val lazyHeader: LazyHeaders,
    private val context: Context,
): ListAdapter<User, UserAdapter.UserViewHolder>(UserAdapter) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding, context, lazyHeader)
    }

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int,
    ) {
        val currentItem = getItem(position)
        if (currentItem != null)
            holder.bind(currentItem)
    }


    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val context: Context,
        private val lazyHeader: LazyHeaders
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.apply {
                this.user = user
                executePendingBindings()
            }

            Glide.with(context)
                .load(GlideUrl(user.getImageUrl(), lazyHeader))
                .into(binding.imgUser)
        }

    }

    companion object DiffCallback : DiffUtil.ItemCallback<User>() {

        override fun areItemsTheSame(oldItem: User, newItem: User) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: User, newItem: User) =
            oldItem == newItem

    }
}