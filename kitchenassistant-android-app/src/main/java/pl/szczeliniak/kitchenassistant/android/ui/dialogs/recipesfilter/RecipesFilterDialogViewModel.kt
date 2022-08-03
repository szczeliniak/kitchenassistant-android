package pl.szczeliniak.kitchenassistant.android.ui.dialogs.recipesfilter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Category
import pl.szczeliniak.kitchenassistant.android.services.RecipeService
import javax.inject.Inject

@HiltViewModel
class RecipesFilterDialogViewModel @Inject constructor(
    private val recipeService: RecipeService
) : ViewModel() {

    private val _categories = MutableLiveData<LoadingState<List<Category>>>()
    private val _tags = MutableLiveData<LoadingState<List<String>>>()

    val categories: LiveData<LoadingState<List<Category>>>
        get() = _categories

    val tags: LiveData<LoadingState<List<String>>>
        get() {
            return _tags
        }

    init {
        reloadCategories()
        loadTags()
    }

    private fun reloadCategories() {
        viewModelScope.launch {
            recipeService.findAllCategories()
                .onEach { _categories.value = it }
                .launchIn(viewModelScope)
        }
    }

    private fun loadTags() {
        viewModelScope.launch {
            recipeService.findAllTags()
                .onEach { _tags.value = it }
                .launchIn(viewModelScope)
        }
    }

}