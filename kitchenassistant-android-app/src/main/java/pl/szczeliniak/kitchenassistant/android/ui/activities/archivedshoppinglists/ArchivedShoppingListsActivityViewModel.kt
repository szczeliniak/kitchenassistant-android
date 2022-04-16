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
import pl.szczeliniak.kitchenassistant.android.network.responses.ShoppingListsResponse
import pl.szczeliniak.kitchenassistant.android.services.ShoppingListService
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.shoppinglistsfilter.ShoppingListsFilterDialog
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ArchivedShoppingListsActivityViewModel @Inject constructor(
    private val shoppingListService: ShoppingListService
) : ViewModel() {

    companion object {
        private const val LIMIT = 20
    }

    private val _shoppingLists = MutableLiveData<LoadingState<ShoppingListsResponse>>()
    private val _filter = MutableLiveData<ShoppingListsFilterDialog.Filter>()

    val shoppingLists: LiveData<LoadingState<ShoppingListsResponse>> get() = _shoppingLists
    val filter: LiveData<ShoppingListsFilterDialog.Filter> get() = _filter

    init {
        reloadShoppingLists(1, null, null)
    }

    fun reloadShoppingLists(page: Int, name: String?, date: LocalDate?) {
        viewModelScope.launch {
            shoppingListService.findAll(true, name, date, page, LIMIT)
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

    fun changeFilter(filter: ShoppingListsFilterDialog.Filter) {
        _filter.value = filter
    }

}