package pl.szczeliniak.kitchenassistant.android.ui.dialogs.addingredienttoshoppinglist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.AddShoppingListItemRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.ShoppingListsResponse
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ShoppingList
import pl.szczeliniak.kitchenassistant.android.services.ShoppingListService
import javax.inject.Inject

@HiltViewModel
class AddIngredientToShoppingListDialogViewModel @Inject constructor(
    private val shoppingListService: ShoppingListService,
) : ViewModel() {

    private val _shoppingLists = MutableLiveData<LoadingState<ShoppingListsResponse>>()

    private val _selectedShoppingList = MutableLiveData<ShoppingList?>()

    val selectedShoppingList: LiveData<ShoppingList?>
        get() {
            return _selectedShoppingList
        }

    val shoppingLists: LiveData<LoadingState<ShoppingListsResponse>>
        get() {
            return _shoppingLists
        }

    init {
        loadShoppingLists()
    }

    private fun loadShoppingLists() {
        viewModelScope.launch {
            shoppingListService.findAll(false, null, null, null, 10)
                .onEach { _shoppingLists.value = it }
                .launchIn(viewModelScope)
        }
    }

    fun addItem(shoppingListId: Int, item: AddShoppingListItemRequest): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            shoppingListService.addShoppingListItem(shoppingListId, item)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    fun setShoppingList(shoppingList: ShoppingList?) {
        _selectedShoppingList.value = shoppingList
    }

}