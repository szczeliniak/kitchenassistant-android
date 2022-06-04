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
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Photo
import pl.szczeliniak.kitchenassistant.android.services.ReceiptService
import javax.inject.Inject

@HiltViewModel
class ReceiptInfoFragmentViewModel @Inject constructor(
    private val receiptService: ReceiptService
) : ViewModel() {

    fun loadPhoto(photo: Photo): LiveData<LoadingState<ReceiptService.DownloadedPhoto>> {
        val liveData = MutableLiveData<LoadingState<ReceiptService.DownloadedPhoto>>()
        viewModelScope.launch {
            receiptService.downloadPhoto(photo)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData;
    }

}