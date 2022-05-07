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
import pl.szczeliniak.kitchenassistant.android.network.responses.ReceiptsResponse
import pl.szczeliniak.kitchenassistant.android.services.ReceiptService
import javax.inject.Inject

@HiltViewModel
class ReceiptsFragmentViewModel @Inject constructor(
    private val receiptService: ReceiptService
) : ViewModel() {

    companion object {
        private const val LIMIT = 20
    }

    private val _receipts = MutableLiveData<LoadingState<ReceiptsResponse>>()

    val receipts: LiveData<LoadingState<ReceiptsResponse>> get() = _receipts

    init {
        loadReceipts(1, null, null, null)
    }

    fun loadReceipts(page: Int, categoryId: Int?, receiptName: String?, tag: String?) {
        viewModelScope.launch {
            receiptService.findAll(categoryId, receiptName, tag, page, LIMIT)
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