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
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Category
import pl.szczeliniak.kitchenassistant.android.services.FileService
import pl.szczeliniak.kitchenassistant.android.services.ReceiptService
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AddEditReceiptActivityViewModel @Inject constructor(
    private val receiptService: ReceiptService,
    private val fileService: FileService
) : ViewModel() {

    private val _categories = MutableLiveData<LoadingState<List<Category>>>()
    private val _tags = MutableLiveData<LoadingState<List<String>>>()

    val categories: LiveData<LoadingState<List<Category>>>
        get() {
            return _categories
        }

    val tags: LiveData<LoadingState<List<String>>>
        get() {
            return _tags
        }

    init {
        loadCategories()
        loadTags()
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

    fun uploadPhotos(file: List<File>): LiveData<LoadingState<List<Int>>> {
        val liveData = MutableLiveData<LoadingState<List<Int>>>()
        viewModelScope.launch {
            fileService.upload(file)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    fun loadFile(id: Int): LiveData<LoadingState<FileService.DownloadedFile>> {
        val liveData = MutableLiveData<LoadingState<FileService.DownloadedFile>>()
        viewModelScope.launch {
            fileService.download(id)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData;
    }

}