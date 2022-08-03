package pl.szczeliniak.kitchenassistant.android.ui.dialogs.addeditcategory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.AddCategoryRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateCategoryRequest
import pl.szczeliniak.kitchenassistant.android.services.RecipeService
import javax.inject.Inject

@HiltViewModel
class AddEditCategoryDialogViewModel @Inject constructor(
    private val recipeService: RecipeService,
) : ViewModel() {

    fun addCategory(request: AddCategoryRequest): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            recipeService.addCategory(request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    fun updateCategory(categoryId: Int, request: UpdateCategoryRequest): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            recipeService.updateCategory(categoryId, request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

}