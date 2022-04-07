package pl.szczeliniak.kitchenassistant.android.ui.activities.addeditreceipt

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.AddReceiptRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateReceiptRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Receipt
import pl.szczeliniak.kitchenassistant.android.services.ReceiptService
import javax.inject.Inject

@HiltViewModel
class AddEditReceiptActivityViewModel @Inject constructor(
    private val receiptService: ReceiptService,
) : ViewModel() {

    private val _receipt = MutableLiveData<LoadingState<Receipt>>()

    val receipt: LiveData<LoadingState<Receipt>>
        get() = _receipt

    fun addReceipt(request: AddReceiptRequest): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            receiptService.add(request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    fun updateReceipt(receiptId: Int, request: UpdateReceiptRequest): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            receiptService.update(receiptId, request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    fun load(receiptId: Int) {
        viewModelScope.launch {
            receiptService.findById(receiptId)
                .onEach { _receipt.value = it }
                .launchIn(viewModelScope)
        }
    }

}