package pl.szczeliniak.kitchenassistant.android.ui.activities.addshoppinglist

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.AddShoppingListRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateShoppingListRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ShoppingListDetails
import pl.szczeliniak.kitchenassistant.android.services.ShoppingListService

class AddEditShoppingListActivityViewModel @AssistedInject constructor(
    private val shoppingListService: ShoppingListService,
    @Assisted private val shoppingListId: Int?
) : ViewModel() {

    private val _shoppingList = MutableLiveData<LoadingState<ShoppingListDetails>>()

    val shoppingList: LiveData<LoadingState<ShoppingListDetails>>
        get() = _shoppingList

    init {
        if (shoppingListId != null) {
            viewModelScope.launch {
                shoppingListService.findById(shoppingListId)
                    .onEach { _shoppingList.value = it }
                    .launchIn(viewModelScope)
            }
        }
    }

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

    @AssistedFactory
    interface Factory {
        fun create(shoppingListId: Int?): AddEditShoppingListActivityViewModel
    }

    companion object {
        fun provideFactory(factory: Factory, shoppingListId: Int?): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return factory.create(shoppingListId) as T
                }
            }
    }

}