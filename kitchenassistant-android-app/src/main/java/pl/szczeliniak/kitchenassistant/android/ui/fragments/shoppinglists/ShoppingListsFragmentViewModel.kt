package pl.szczeliniak.kitchenassistant.android.ui.fragments.shoppinglists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.responses.ShoppingListsResponse
import pl.szczeliniak.kitchenassistant.android.services.ShoppingListService
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ShoppingListsFragmentViewModel @Inject constructor(
    private val shoppingListService: ShoppingListService
) : ViewModel() {

    companion object {
        private const val LIMIT = 20
    }

    private val _shoppingLists = MutableLiveData<LoadingState<ShoppingListsResponse>>()

    val shoppingLists: LiveData<LoadingState<ShoppingListsResponse>> get() = _shoppingLists

    init {
        reloadShoppingLists(1, null, null)
    }

    fun reloadShoppingLists(page: Int, name: String?, date: LocalDate?) {
        viewModelScope.launch {
            shoppingListService.findAll(false, name, date, page, LIMIT)
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

    fun archive(id: Int): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            shoppingListService.archive(id, true)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

}