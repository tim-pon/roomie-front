package com.example.roomie.presentation.shopping.list_detail

import androidx.lifecycle.*
import com.example.roomie.core.Constants.PARAM_SHOPPING_LIST_ID
import com.example.roomie.data.repository.ArticleRepository
import com.example.roomie.data.repository.ShoppingListRepository
import com.example.roomie.domain.model.Article
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShoppingListDetailViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Trigger value to reload articles (used for swipeOnRefresh layout)
    private val reloadTrigger = MutableLiveData<Boolean>(false)
    private var forceFetch = false

    // Shopping list id passed from ShoppingFragment
    val shoppingListId: Int = savedStateHandle.get<Int>(PARAM_SHOPPING_LIST_ID)!!

    // Articles of shoppinglist
    var articles = reloadTrigger.switchMap {
        articleRepository.getArticlesByShoppingListId(shoppingListId, forceFetch)}


    // Initial trigger to load shoppinglists
    init { refresh(false)}


    /**
     * Triggers reload
     * @param forceFetch determines local or remote reload
     */
    fun refresh(forceFetch: Boolean) {
        this.forceFetch = forceFetch
        reloadTrigger.value = true
    }

    fun deleteArticle(article: Article) = articleRepository.deleteArticle(article)

    fun tickArticle(article: Article) = articleRepository.tickArticle(article)

}