package pl.szczeliniak.kitchenassistant.android.ui.fragments.recipesbycategory

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.AddRecipeToDayPlanRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.RecipesResponse
import pl.szczeliniak.kitchenassistant.android.services.DayPlanService
import pl.szczeliniak.kitchenassistant.android.services.PhotoService
import pl.szczeliniak.kitchenassistant.android.services.RecipeService

class RecipesByCategoryFragmentViewModel @AssistedInject constructor(
    private val recipeService: RecipeService,
    private val dayPlanService: DayPlanService,
    private val photoService: PhotoService,
    @Assisted private val categoryId: Int?
) : ViewModel() {

    companion object {
        private const val LIMIT = 20

        fun provideFactory(factory: Factory, categoryId: Int?): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return factory.create(categoryId) as T
                }
            }
    }

    private val _recipes = MutableLiveData<LoadingState<RecipesResponse>>()

    val recipes: LiveData<LoadingState<RecipesResponse>> get() = _recipes

    init {
        loadRecipes(1, null, null, false)
    }

    fun loadRecipes(page: Int, recipeName: String?, tag: String?, onlyFavorites: Boolean) {
        viewModelScope.launch {
            recipeService.findAll(categoryId, recipeName, tag, onlyFavorites, page, LIMIT)
                .onEach { _recipes.value = it }
                .launchIn(viewModelScope)
        }
    }

    fun delete(id: Int): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            recipeService.delete(id)
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
        return liveData;
    }

    fun setFavorite(id: Int, isFavorite: Boolean): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            recipeService.setFavorite(id, isFavorite)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    fun assignRecipeToDayPlan(recipeId: Int, request: AddRecipeToDayPlanRequest): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            dayPlanService.assignRecipe(recipeId, request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    @AssistedFactory
    interface Factory {
        fun create(categoryId: Int?): RecipesByCategoryFragmentViewModel
    }

}