package pl.szczeliniak.cookbook.android.ui.fragments.recipesbycategory

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.cookbook.android.network.LoadingState
import pl.szczeliniak.cookbook.android.network.requests.AddRecipeToDayPlanRequest
import pl.szczeliniak.cookbook.android.network.responses.RecipesResponse
import pl.szczeliniak.cookbook.android.services.DayPlanService
import pl.szczeliniak.cookbook.android.services.RecipeService

class RecipesByCategoryFragmentViewModel @AssistedInject constructor(
    private val recipeService: RecipeService,
    private val dayPlanService: DayPlanService,
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
        loadRecipes(1, null)
    }

    fun loadRecipes(page: Long, search: String?) {
        viewModelScope.launch {
            recipeService.findAll(categoryId, search, page, LIMIT)
                .onEach { _recipes.value = it }
                .launchIn(viewModelScope)
        }
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

    fun assignRecipeToDayPlan(request: AddRecipeToDayPlanRequest): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            dayPlanService.addRecipe(request)
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