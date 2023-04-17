package pl.szczeliniak.kitchenassistant.android.ui.fragments.recipeinfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.services.PhotoService
import javax.inject.Inject

@HiltViewModel
class RecipeInfoFragmentViewModel @Inject constructor(
    private val photoService: PhotoService
) : ViewModel() {

    fun loadPhoto(photoName: String): LiveData<LoadingState<PhotoService.DownloadedPhoto>> {
        val liveData = MutableLiveData<LoadingState<PhotoService.DownloadedPhoto>>()
        viewModelScope.launch {
            photoService.downloadPhoto(photoName)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData;
    }

}