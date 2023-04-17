package pl.szczeliniak.kitchenassistant.android.ui.activities.addeditrecipe

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.AddRecipeRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateRecipeRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Category
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.RecipeDetails
import pl.szczeliniak.kitchenassistant.android.services.PhotoService
import pl.szczeliniak.kitchenassistant.android.services.RecipeService
import java.io.File

class AddEditRecipeActivityViewModel @AssistedInject constructor(
    private val recipeService: RecipeService,
    private val photoService: PhotoService,
    @Assisted private val recipeId: Int?
) : ViewModel() {

    private val _categories = MutableLiveData<LoadingState<List<Category>>>()
    private val _tags = MutableLiveData<LoadingState<List<String>>>()
    private val _authors = MutableLiveData<LoadingState<List<String>>>()
    private val _recipe = MutableLiveData<LoadingState<RecipeDetails>>()

    val recipe: LiveData<LoadingState<RecipeDetails>>
        get() = _recipe

    val categories: LiveData<LoadingState<List<Category>>>
        get() {
            return _categories
        }

    val tags: LiveData<LoadingState<List<String>>>
        get() {
            return _tags
        }

    val authors: LiveData<LoadingState<List<String>>>
        get() {
            return _authors
        }

    init {
        loadRecipe()
        loadCategories()
        loadTags()
        loadAuthors()
    }

    fun addRecipe(request: AddRecipeRequest): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            recipeService.add(request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    fun updateRecipe(recipeId: Int, request: UpdateRecipeRequest): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            recipeService.update(recipeId, request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    private fun loadCategories() {
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

    private fun loadAuthors() {
        viewModelScope.launch {
            recipeService.findAllAuthors()
                .onEach { _authors.value = it }
                .launchIn(viewModelScope)
        }
    }

    fun uploadPhoto(file: File): LiveData<LoadingState<String>> {
        val liveData = MutableLiveData<LoadingState<String>>()
        viewModelScope.launch {
            photoService.uploadPhoto(file)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    fun loadPhoto(photoName: String): LiveData<LoadingState<PhotoService.DownloadedPhoto>> {
        val liveData = MutableLiveData<LoadingState<PhotoService.DownloadedPhoto>>()
        viewModelScope.launch {
            photoService.downloadPhoto(photoName)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    private fun loadRecipe() {
        recipeId?.let {
            viewModelScope.launch {
                recipeService.findById(it)
                    .onEach { _recipe.value = it }
                    .launchIn(viewModelScope)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(recipeId: Int?): AddEditRecipeActivityViewModel
    }

    companion object {
        fun provideFactory(factory: Factory, recipeId: Int?): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return factory.create(recipeId) as T
                }
            }
    }

}