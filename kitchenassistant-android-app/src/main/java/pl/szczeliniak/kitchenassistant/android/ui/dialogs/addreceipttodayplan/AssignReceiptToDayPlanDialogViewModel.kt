package pl.szczeliniak.kitchenassistant.android.ui.dialogs.addreceipttodayplan

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
class AssignReceiptToDayPlanDialogViewModel @Inject constructor(
    private val receiptService: ReceiptService
) : ViewModel() {

    private val _receipts = MutableLiveData<LoadingState<ReceiptsResponse>>()

    val receipts: LiveData<LoadingState<ReceiptsResponse>>
        get() = _receipts

    init {
        reloadReceipts(null)
    }

    fun reloadReceipts(name: String?) {
        viewModelScope.launch {
            receiptService.findAll(receiptName = name)
                .onEach { _receipts.value = it }
                .launchIn(viewModelScope)
        }
    }

}