package pl.szczeliniak.kitchenassistant.android.ui.activities.receipt

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Receipt
import pl.szczeliniak.kitchenassistant.android.services.ReceiptService

class ReceiptActivityViewModel @AssistedInject constructor(
    private val receiptService: ReceiptService,
    @Assisted private val receiptId: Int
) : ViewModel() {

    private val _receipt = MutableLiveData<LoadingState<Receipt>>()

    val receipt: LiveData<LoadingState<Receipt>>
        get() = _receipt

    fun reload() {
        viewModelScope.launch {
            receiptService.findById(receiptId)
                .onEach { _receipt.value = it }
                .launchIn(viewModelScope)
        }
    }

    init {
        reload()
    }

    @AssistedFactory
    interface Factory {
        fun create(receiptId: Int) : ReceiptActivityViewModel
    }

    companion object {
        fun provideFactory(factory: Factory, receiptId: Int): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return factory.create(receiptId) as T
            }
        }

    }

}