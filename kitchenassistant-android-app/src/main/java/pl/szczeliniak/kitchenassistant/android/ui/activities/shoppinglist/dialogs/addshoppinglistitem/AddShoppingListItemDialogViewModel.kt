package pl.szczeliniak.kitchenassistant.android.ui.activities.shoppinglist.dialogs.addshoppinglistitem

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
import pl.szczeliniak.kitchenassistant.android.services.ShoppingListService
import javax.inject.Inject

@HiltViewModel
class AddShoppingListItemDialogViewModel @Inject constructor(
    private val shoppingListService: ShoppingListService,
) : ViewModel() {

    fun addShoppingListItem(shoppingListId: Int, request: AddShoppingListItemRequest): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            shoppingListService.addShoppingListItem(shoppingListId, request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

}