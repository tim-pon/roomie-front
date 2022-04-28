package com.example.roomie.presentation.shopping.overview

import androidx.lifecycle.*
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.core.Resource
import com.example.roomie.data.repository.ShoppingListRepository
import com.example.roomie.domain.model.ShoppingList
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShoppingListsViewModel @Inject constructor(
    val repository: ShoppingListRepository
) : ViewModel() {

    // Trigger value to reload shoppinglists (used for swipeOnRefresh layout)
    private val reloadTrigger = MutableLiveData<Boolean>()
    var forceFetch = false

    private val flatId = FlatStorage.getFlatId()!!

    // Holding all shopping lists
    val shoppingLists: LiveData<Resource<List<ShoppingList>>> =
        Transformations.switchMap(reloadTrigger) {
            repository.getShoppingListsByFlatId(flatId, forceFetch)}


    // Initial trigger to load shoppinglists
    init { refresh(false) }


    /**
     * Triggers reload
     * @param forceFetch determines local or remote reload
     */
    fun refresh(forceFetch: Boolean) {
        this.forceFetch = forceFetch
        reloadTrigger.value = true
    }

    fun createShoppingList(name: String) = repository.createShoppingList(ShoppingList.init(flatId, name))

    fun setShoppingList(shoppingList: ShoppingList) = repository.setShoppingList(shoppingList)

    fun deleteShoppingList(shoppingList: ShoppingList) = repository.deleteShoppingList(shoppingList)
}