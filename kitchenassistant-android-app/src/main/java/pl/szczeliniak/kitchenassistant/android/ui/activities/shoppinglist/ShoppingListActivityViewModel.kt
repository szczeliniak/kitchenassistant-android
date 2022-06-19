package pl.szczeliniak.kitchenassistant.android.ui.activities.shoppinglist

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ShoppingList
import pl.szczeliniak.kitchenassistant.android.services.ShoppingListService

class ShoppingListActivityViewModel @AssistedInject constructor(
    private val shoppingListService: ShoppingListService,
    @Assisted private val shoppingListId: Int
) : ViewModel() {

    private val _shoppingList = MutableLiveData<LoadingState<ShoppingList>>()

    val shoppingList: LiveData<LoadingState<ShoppingList>>
        get() = _shoppingList

    init {
        reload()
    }

    fun reload() {
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

    @AssistedFactory
    interface Factory {
        fun create(shoppingListId: Int): ShoppingListActivityViewModel
    }

    companion object {
        fun provideFactory(factory: Factory, shoppingListId: Int): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return factory.create(shoppingListId) as T
                }
            }
    }

}