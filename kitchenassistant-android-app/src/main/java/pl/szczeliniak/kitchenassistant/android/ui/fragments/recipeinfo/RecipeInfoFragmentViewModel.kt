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
import pl.szczeliniak.kitchenassistant.android.services.RecipeService
import javax.inject.Inject

@HiltViewModel
class RecipeInfoFragmentViewModel @Inject constructor(
    private val recipeService: RecipeService
) : ViewModel() {

    fun loadPhoto(recipeId: Int, photoName: String): LiveData<LoadingState<RecipeService.DownloadedPhoto>> {
        val liveData = MutableLiveData<LoadingState<RecipeService.DownloadedPhoto>>()
        viewModelScope.launch {
            recipeService.downloadPhoto(photoName, recipeId)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData;
    }

}