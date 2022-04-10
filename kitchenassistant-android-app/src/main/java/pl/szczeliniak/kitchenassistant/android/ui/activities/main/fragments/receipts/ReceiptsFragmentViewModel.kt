package pl.szczeliniak.kitchenassistant.android.ui.activities.main.fragments.receipts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Receipt
import pl.szczeliniak.kitchenassistant.android.services.ReceiptService
import javax.inject.Inject

@HiltViewModel
class ReceiptsFragmentViewModel @Inject constructor(
    private val receiptService: ReceiptService
) : ViewModel() {

    private val _receipts = MutableLiveData<LoadingState<List<Receipt>>>()

    val receipts: LiveData<LoadingState<List<Receipt>>>
        get() = _receipts

    init {
        reloadReceipts()
    }

    fun reloadReceipts() {
        viewModelScope.launch {
            receiptService.findAll()
                .onEach { _receipts.value = it }
                .launchIn(viewModelScope)
        }
    }

    fun delete(id: Int): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            receiptService.delete(id)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

}