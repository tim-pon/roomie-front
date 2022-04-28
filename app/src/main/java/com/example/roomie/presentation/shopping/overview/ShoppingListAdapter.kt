package com.example.roomie.presentation.shopping.overview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.roomie.databinding.ItemShoppingListBinding
import com.example.roomie.domain.model.ShoppingList

class ShoppingListAdapter (
    private val onUpdateCallback: (ShoppingList) -> Unit
): ListAdapter<ShoppingList, ShoppingListAdapter.ShoppingListViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListViewHolder {
        val binding = ItemShoppingListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ShoppingListViewHolder(binding, onUpdateCallback)
    }

    override fun onBindViewHolder(holder: ShoppingListViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null)
            holder.bind(currentItem)
    }


    inner class ShoppingListViewHolder(
        private val binding: ItemShoppingListBinding,
        onUpdateCallback: (ShoppingList) -> Unit
        ) : RecyclerView.ViewHolder(binding.root) {

        init {
            // click on cards navigates to shopping detail fragment
            binding.setClickListener { view ->
                binding.shoppingList?.let { shoppingList ->
                    navigateToShoppingListDetail(shoppingList, view)}}

            binding.btnMore.setOnClickListener { view ->
                onUpdateCallback(binding.shoppingList!!)
                notifyItemChanged(adapterPosition)
            }
        }

        private fun navigateToShoppingListDetail(shoppingList: ShoppingList,view: View) {
            val direction = ShoppingFragmentDirections
                .actionNavigationShoppingToShoppingListDetailFragment(shoppingList.id, shoppingList.name)
            view.findNavController().navigate(direction)
        }

        fun bind(shoppingList: ShoppingList) {
            binding.apply {
                this.shoppingList = shoppingList
                executePendingBindings()}
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ShoppingList>() {

        override fun areItemsTheSame(oldItem: ShoppingList, newItem: ShoppingList) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ShoppingList, newItem: ShoppingList) =
            oldItem == newItem

    }
}