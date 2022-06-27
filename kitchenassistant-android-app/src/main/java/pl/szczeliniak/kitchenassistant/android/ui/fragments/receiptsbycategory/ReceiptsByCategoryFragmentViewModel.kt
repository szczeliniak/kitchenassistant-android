package pl.szczeliniak.kitchenassistant.android.ui.fragments.receiptsbycategory

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.responses.ReceiptsResponse
import pl.szczeliniak.kitchenassistant.android.services.ReceiptService

class ReceiptsByCategoryFragmentViewModel @AssistedInject constructor(
    private val receiptService: ReceiptService,
    @Assisted private val categoryId: Int?
) : ViewModel() {

    companion object {
        private const val LIMIT = 20

        fun provideFactory(factory: Factory, categoryId: Int?): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return factory.create(categoryId) as T
                }
            }
    }

    private val _receipts = MutableLiveData<LoadingState<ReceiptsResponse>>()

    val receipts: LiveData<LoadingState<ReceiptsResponse>> get() = _receipts

    init {
        loadReceipts(1, null, null)
    }

    fun loadReceipts(page: Int, receiptName: String?, tag: String?) {
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

    fun setFavorite(id: Int, isFavorite: Boolean): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            receiptService.setFavorite(id, isFavorite)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    @AssistedFactory
    interface Factory {
        fun create(categoryId: Int?): ReceiptsByCategoryFragmentViewModel
    }

}