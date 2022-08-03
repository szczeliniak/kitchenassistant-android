package pl.szczeliniak.kitchenassistant.android.ui.fragments.recipesteps

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
class RecipeStepsFragmentViewModel @Inject constructor(
    private val recipeService: RecipeService,
) : ViewModel() {

    fun delete(recipeId: Int, stepId: Int): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            recipeService.deleteStep(recipeId, stepId)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

}