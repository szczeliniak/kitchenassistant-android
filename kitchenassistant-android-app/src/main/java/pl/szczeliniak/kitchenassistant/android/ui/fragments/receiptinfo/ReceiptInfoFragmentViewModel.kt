package pl.szczeliniak.kitchenassistant.android.ui.fragments.receiptinfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.services.FileService
import javax.inject.Inject

@HiltViewModel
class ReceiptInfoFragmentViewModel @Inject constructor(
    private val fileService: FileService
) : ViewModel() {

    fun loadPhoto(id: Int): LiveData<LoadingState<FileService.DownloadedFile>> {
        val liveData = MutableLiveData<LoadingState<FileService.DownloadedFile>>()
        viewModelScope.launch {
            fileService.download(id)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData;
    }

}