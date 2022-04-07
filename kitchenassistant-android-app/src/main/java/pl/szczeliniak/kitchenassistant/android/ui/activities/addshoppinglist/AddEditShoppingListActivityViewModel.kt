package pl.szczeliniak.kitchenassistant.android.ui.activities.addshoppinglist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.AddShoppingListRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateShoppingListRequest
import pl.szczeliniak.kitchenassistant.android.services.ShoppingListService
import javax.inject.Inject

@HiltViewModel
class AddEditShoppingListActivityViewModel @Inject constructor(
    private val shoppingListService: ShoppingListService,
) : ViewModel() {

    fun addShoppingList(request: AddShoppingListRequest): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            shoppingListService.add(request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    fun updateShoppingList(shoppingListId: Int, request: UpdateShoppingListRequest): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            shoppingListService.update(shoppingListId, request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

}