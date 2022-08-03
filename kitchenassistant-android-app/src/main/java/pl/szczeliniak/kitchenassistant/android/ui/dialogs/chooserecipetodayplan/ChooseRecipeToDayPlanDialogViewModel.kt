package pl.szczeliniak.kitchenassistant.android.ui.dialogs.chooserecipetodayplan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.responses.RecipesResponse
import pl.szczeliniak.kitchenassistant.android.services.RecipeService
import javax.inject.Inject

@HiltViewModel
class ChooseRecipeToDayPlanDialogViewModel @Inject constructor(
    private val recipeService: RecipeService
) : ViewModel() {

    private val _recipes = MutableLiveData<LoadingState<RecipesResponse>>()

    val recipes: LiveData<LoadingState<RecipesResponse>>
        get() = _recipes

    init {
        reloadRecipes(null)
    }

    fun reloadRecipes(name: String?) {
        viewModelScope.launch {
            recipeService.findAll(recipeName = name)
                .onEach { _recipes.value = it }
                .launchIn(viewModelScope)
        }
    }

}