package pl.szczeliniak.kitchenassistant.android.ui.activities.shoppinglist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ShoppingList
import pl.szczeliniak.kitchenassistant.android.services.ShoppingListService
import javax.inject.Inject

@HiltViewModel
class ShoppingListActivityViewModel @Inject constructor(
    private val shoppingListService: ShoppingListService,
) : ViewModel() {

    private val _shoppingList = MutableLiveData<LoadingState<ShoppingList>>()

    val shoppingList: LiveData<LoadingState<ShoppingList>>
        get() = _shoppingList

    fun load(shoppingListId: Int) {
        viewModelScope.launch {
            shoppingListService.findById(shoppingListId)
                .onEach { _shoppingList.value = it }
                .launchIn(viewModelScope)
        }
    }

    fun deleteItem(shoppingListId: Int, shoppingListItemId: Int): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            shoppingListService.deleteShoppingListItem(shoppingListId, shoppingListItemId)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    fun changeItemState(shoppingListId: Int, shoppingListItemId: Int, state: Boolean): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            shoppingListService.changeItemState(shoppingListId, shoppingListItemId, state)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    fun archive(shoppingListId: Int): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            shoppingListService.archive(shoppingListId, true)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData

    }


}