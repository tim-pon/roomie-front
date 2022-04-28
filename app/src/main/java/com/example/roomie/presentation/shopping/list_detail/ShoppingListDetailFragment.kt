package com.example.roomie.presentation.shopping.list_detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.roomie.databinding.FragmentShoppingDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.roomie.core.Status
import com.example.roomie.domain.model.Article
import com.example.roomie.presentation.CustomSnackbar
import com.example.roomie.presentation.SwipeToDelete

@AndroidEntryPoint
class ShoppingListDetailFragment: Fragment() {

    private lateinit var binding: FragmentShoppingDetailBinding
    private val viewModel: ShoppingListDetailViewModel by viewModels()
    private var adapter = ArticleAdapter(::tickArticle)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShoppingDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@ShoppingListDetailFragment.viewModel
            recyclerViewArticleList.adapter = initAdapter()
            swiperefresh.setOnRefreshListener {
                this@ShoppingListDetailFragment.viewModel.refresh(true) }
            executePendingBindings()
        }

        // Navigates to article fragment
        binding.btnAddArticle.setOnClickListener { createArticle(it) }

        val itemTouchHelper = ItemTouchHelper(SwipeToDelete(requireContext(), ::deleteArticle))
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewArticleList)

    }


    /**
     * Subscribes articles livedata to ui
     * @return adapter with list of articles
     */
    private fun initAdapter(): ArticleAdapter {

        viewModel.articles.observe(viewLifecycleOwner) { articles ->

            adapter.submitList(articles.data?.sortedBy { it.bought })

            binding.isEmpty = articles.data.isNullOrEmpty() && articles.status !== Status.LOADING
            binding.btnEmpty.setOnClickListener { createArticle(it) }

            // Response handling
            binding.swiperefresh.isRefreshing = articles.status == Status.LOADING
            if (articles.status == Status.ERROR)
                CustomSnackbar.defaultError(requireView(), articles.code)
        }
        return adapter
    }

    private fun deleteArticle(adapterPosition: Int) {
        val article = adapter.currentList[adapterPosition]
        viewModel.deleteArticle(article).observe(viewLifecycleOwner) {
            if (it.status == Status.ERROR)
                CustomSnackbar.defaultError(requireView(),it.code)
        }
    }

    private fun tickArticle(article: Article) {
        viewModel.tickArticle(article).observe(viewLifecycleOwner) {
            if (it.status == Status.ERROR)
                CustomSnackbar.defaultError(requireView(),it.code)
        }
    }

    private fun createArticle(view: View) {
        val direction = ShoppingListDetailFragmentDirections
            .actionShoppingListDetailFragmentToArticleFragment(shoppingListId = viewModel.shoppingListId)
        view.findNavController().navigate(direction)
    }

}