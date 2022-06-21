package pl.szczeliniak.kitchenassistant.android.ui.fragments.receipts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Category
import pl.szczeliniak.kitchenassistant.android.services.ReceiptService
import javax.inject.Inject

@HiltViewModel
class ReceiptsFragmentViewModel @Inject constructor(
    private val receiptService: ReceiptService
) : ViewModel() {

    private val _categories = MutableLiveData<LoadingState<List<Category>>>()

    val categories: LiveData<LoadingState<List<Category>>> get() = _categories

    init {
        reloadCategories()
    }

    fun reloadCategories() {
        viewModelScope.launch {
            receiptService.findAllCategories()
                .onEach { _categories.value = it }
                .launchIn(viewModelScope)
        }
    }

}