package pl.szczeliniak.kitchenassistant.android.ui.activities.recipe

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.responses.RecipeResponse
import pl.szczeliniak.kitchenassistant.android.services.RecipeService

class RecipeActivityViewModel @AssistedInject constructor(
    private val recipeService: RecipeService,
    @Assisted private val recipeId: Int
) : ViewModel() {

    private val _recipe = MutableLiveData<LoadingState<RecipeResponse.Recipe>>()

    val recipe: LiveData<LoadingState<RecipeResponse.Recipe>>
        get() = _recipe

    private fun reload() {
        viewModelScope.launch {
            recipeService.findById(recipeId)
                .onEach { _recipe.value = it }
                .launchIn(viewModelScope)
        }
    }

    init {
        reload()
    }

    @AssistedFactory
    interface Factory {
        fun create(recipeId: Int): RecipeActivityViewModel
    }

    companion object {
        fun provideFactory(factory: Factory, recipeId: Int): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return factory.create(recipeId) as T
                }
            }
    }

}