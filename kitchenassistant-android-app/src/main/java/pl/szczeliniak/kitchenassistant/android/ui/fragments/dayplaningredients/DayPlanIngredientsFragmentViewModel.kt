package pl.szczeliniak.kitchenassistant.android.ui.fragments.dayplaningredients

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.DayPlanRecipe
import pl.szczeliniak.kitchenassistant.android.services.DayPlanService

class DayPlanIngredientsFragmentViewModel @AssistedInject constructor(
    private val dayPlanService: DayPlanService,
    @Assisted private val dayPlanId: Int
) : ViewModel() {

    private val _recipes = MutableLiveData<LoadingState<List<DayPlanRecipe>>>()

    val recipes: LiveData<LoadingState<List<DayPlanRecipe>>>
        get() = _recipes

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            dayPlanService.getRecipes(dayPlanId)
                .onEach { _recipes.value = it }
                .launchIn(viewModelScope)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(dayPlanId: Int): DayPlanIngredientsFragmentViewModel
    }

    companion object {
        fun provideFactory(factory: Factory, dayPlanId: Int): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return factory.create(dayPlanId) as T
                }
            }
    }

}