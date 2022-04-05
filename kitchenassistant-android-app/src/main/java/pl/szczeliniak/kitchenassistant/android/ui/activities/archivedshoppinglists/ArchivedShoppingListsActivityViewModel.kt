package pl.szczeliniak.kitchenassistant.android.ui.activities.archivedshoppinglists

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
class ArchivedShoppingListsActivityViewModel @Inject constructor(
    private val shoppingListService: ShoppingListService
) : ViewModel() {

    private val _shoppingLists = MutableLiveData<LoadingState<List<ShoppingList>>>()

    val shoppingLists: LiveData<LoadingState<List<ShoppingList>>>
        get() = _shoppingLists

    fun reloadShoppingLists() {
        viewModelScope.launch {
            shoppingListService.findAll(true)
                .onEach { _shoppingLists.value = it }
                .launchIn(viewModelScope)
        }
    }

    fun delete(id: Int): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            shoppingListService.delete(id)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

}