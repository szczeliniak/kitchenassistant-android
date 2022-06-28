package pl.szczeliniak.kitchenassistant.android.ui.activities.addeditreceipt

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.AddReceiptRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateReceiptRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Category
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Photo
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ReceiptDetails
import pl.szczeliniak.kitchenassistant.android.services.ReceiptService
import java.io.File

class AddEditReceiptActivityViewModel @AssistedInject constructor(
    private val receiptService: ReceiptService,
    @Assisted private val receiptId: Int?
) : ViewModel() {

    private val _categories = MutableLiveData<LoadingState<List<Category>>>()
    private val _tags = MutableLiveData<LoadingState<List<String>>>()
    private val _authors = MutableLiveData<LoadingState<List<String>>>()
    private val _receipt = MutableLiveData<LoadingState<ReceiptDetails>>()

    val receipt: LiveData<LoadingState<ReceiptDetails>>
        get() = _receipt

    val categories: LiveData<LoadingState<List<Category>>>
        get() {
            return _categories
        }

    val tags: LiveData<LoadingState<List<String>>>
        get() {
            return _tags
        }

    val authors: LiveData<LoadingState<List<String>>>
        get() {
            return _authors
        }

    init {
        loadReceipt()
        loadCategories()
        loadTags()
        loadAuthors()
    }

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

    private fun loadCategories() {
        viewModelScope.launch {
            receiptService.findAllCategories()
                .onEach { _categories.value = it }
                .launchIn(viewModelScope)
        }
    }

    private fun loadTags() {
        viewModelScope.launch {
            receiptService.findAllTags()
                .onEach { _tags.value = it }
                .launchIn(viewModelScope)
        }
    }

    private fun loadAuthors() {
        viewModelScope.launch {
            receiptService.findAllAuthors()
                .onEach { _authors.value = it }
                .launchIn(viewModelScope)
        }
    }

    fun uploadPhotos(file: List<File>): LiveData<LoadingState<List<Int>>> {
        val liveData = MutableLiveData<LoadingState<List<Int>>>()
        viewModelScope.launch {
            receiptService.uploadPhoto(file)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    fun loadPhoto(photo: Photo): LiveData<LoadingState<ReceiptService.DownloadedPhoto>> {
        val liveData = MutableLiveData<LoadingState<ReceiptService.DownloadedPhoto>>()
        viewModelScope.launch {
            receiptService.downloadPhoto(photo)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    private fun loadReceipt() {
        receiptId?.let {
            viewModelScope.launch {
                receiptService.findById(it)
                    .onEach { _receipt.value = it }
                    .launchIn(viewModelScope)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(receiptId: Int?): AddEditReceiptActivityViewModel
    }

    companion object {
        fun provideFactory(factory: Factory, receiptId: Int?): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return factory.create(receiptId) as T
                }
            }
    }

}