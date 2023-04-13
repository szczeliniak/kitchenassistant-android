package pl.szczeliniak.kitchenassistant.android.ui.dialogs.addeditingredient

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.AddIngredientGroupRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.EditIngredientGroupRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.IngredientGroup
import pl.szczeliniak.kitchenassistant.android.services.RecipeService

class AddEditIngredientGroupDialogViewModel @AssistedInject constructor(
    private val recipeService: RecipeService,
    @Assisted val recipeId: Int,
    @Assisted val ingredientGroupId: Int?
) : ViewModel() {

    private val _ingredientGroup = MutableLiveData<LoadingState<IngredientGroup>>()

    val ingredientGroup: LiveData<LoadingState<IngredientGroup>>
        get() = _ingredientGroup

    init {
        reload()
    }

    private fun reload() {
        ingredientGroupId?.let {
            viewModelScope.launch {
                recipeService.getIngredientGroupById(recipeId, it)
                    .onEach { _ingredientGroup.value = it }
                    .launchIn(viewModelScope)
            }
        }
    }

    fun editIngredientGroup(
        recipeId: Int,
        groupId: Int,
        request: EditIngredientGroupRequest
    ): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            recipeService.editIngredientGroup(recipeId, groupId, request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    fun addIngredientGroup(recipeId: Int, request: AddIngredientGroupRequest): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            recipeService.addIngredientGroup(recipeId, request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    @AssistedFactory
    interface Factory {
        fun create(recipeId: Int, ingredientGroupId: Int?): AddEditIngredientGroupDialogViewModel
    }

    companion object {
        fun provideFactory(factory: Factory, recipeId: Int, ingredientGroupId: Int?): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return factory.create(recipeId, ingredientGroupId) as T
                }
            }
    }

}