package com.example.roomie.presentation.shopping.article_detail

import android.util.Log
import androidx.lifecycle.*
import com.example.roomie.core.Constants.TMP_ID
import com.example.roomie.core.Resource
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.data.repository.ArticleRepository
import com.example.roomie.data.repository.ShoppingListRepository
import com.example.roomie.domain.model.Article
import com.example.roomie.domain.model.ShoppingList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Article id passed from ShoppingDetailFragment
    // Default id of -1 is used to create a new article
    private val articleId: Int = savedStateHandle.get<Int>("articleId")!!

    // ShoppingList id passed from ShoppingDetailFragment
    // required to create new articles in the corresponding shoppinglist
    private val shoppingListId: Int = savedStateHandle.get<Int>("shoppingListId")!!
    

    // Current article
    var article: LiveData<Resource<Article>> = if (articleId == TMP_ID) {
        MutableLiveData(Resource.loading(Article.init(shoppingListId)))
    } else {
        articleRepository.getArticleById(articleId)
    }


    fun setArticle() =
        if (article.value!!.data!!.id == TMP_ID) {
            // create (POST)
            articleRepository.createArticle(FlatStorage.getFlatId(), article.value!!.data!!)
        } else
            // update (PUT)
            articleRepository.setArticle(article.value!!.data!!)
}