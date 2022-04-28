package com.example.roomie.presentation.shopping.list_detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.roomie.databinding.ItemArticleBinding
import com.example.roomie.domain.model.Article

class ArticleAdapter (
    private val onTickCallback: (Article) -> Unit
): ListAdapter<Article, ArticleAdapter.ArticleViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemArticleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding, onTickCallback)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null)
            holder.bind(currentItem)
    }


    class ArticleViewHolder (
        private val binding: ItemArticleBinding,
        private val onTickCallback: (Article) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {


        init {
            // Click on more navigates to article fragment
            binding.btnMore.setOnClickListener { view ->
                binding.article?.let { article ->
                    navigateToArticleDialog(article.id, article.shoppingListId, view)}
            }

            binding.chkArticleBought.setOnClickListener {
                setArticleUI()
                binding.article?.let { onTickCallback(it) }
            }
        }


        fun bind(article: Article) {
            binding.apply {
                this.article = article
                setArticleUI()
                executePendingBindings()
            }
        }

        private fun setArticleUI() {
            if (binding.article?.bought == true) {
                binding.cardArticle.cardElevation = 0f
            } else {
                binding.cardArticle.cardElevation = 8f
            }
            binding.chkArticleBought.paint.isStrikeThruText = binding.article?.bought?: false
        }

        private fun navigateToArticleDialog(articleId: Int, shoppingListId: Int, view: View) {
            val direction = ShoppingListDetailFragmentDirections
                .actionShoppingListDetailFragmentToArticleFragment(articleId, shoppingListId)
            view.findNavController().navigate(direction)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Article>() {

        override fun areItemsTheSame(oldItem: Article, newItem: Article) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Article, newItem: Article) =
            oldItem == newItem
    }
}