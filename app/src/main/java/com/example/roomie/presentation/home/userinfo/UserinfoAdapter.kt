package com.example.roomie.presentation.home.userinfo

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
import com.example.roomie.databinding.ItemUserinfoBinding
import com.example.roomie.domain.model.User
import de.hdodenhof.circleimageview.CircleImageView
import javax.inject.Inject


class UserinfoAdapter @Inject constructor(
    val context: Context,
    private var lazyHeader: LazyHeaders,
) : ListAdapter<User, UserinfoAdapter.UserinfoViewHolder>(
    UserinfoAdapter
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserinfoViewHolder {
        val binding = ItemUserinfoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return UserinfoViewHolder(binding, context, lazyHeader)
    }

    override fun onBindViewHolder(holder: UserinfoViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null)
            holder.bind(currentItem)
    }

    class UserinfoViewHolder(
        private val binding: ItemUserinfoBinding,
        private val context: Context,
        private val lazyHeader: LazyHeaders,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(userinfo: User) {
            binding.apply {
                this.userinfo = userinfo
                this.userName.text = userinfo.username
                setUserImage(userinfo.id!!, this.userImage)
                executePendingBindings()
            }
        }

        /**
         * set userImage directly into item view with Glide
         * caching is disabled
         * if no picture can be obtained, a placeholder image is set
         */
        private fun setUserImage(userId: Int, view: CircleImageView) {
            val url = Constants.BASE_URL + "user/image/$userId"
            Glide.with(context)
                .load(GlideUrl(url, lazyHeader))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_account)
                .into(view)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<User>() {

        override fun areItemsTheSame(oldItem: User, newItem: User) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: User, newItem: User) =
            oldItem == newItem

    }


}