package pl.szczeliniak.cookbook.android.ui.activities.dayplan

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.cookbook.android.network.LoadingState
import pl.szczeliniak.cookbook.android.network.responses.DayPlanResponse
import pl.szczeliniak.cookbook.android.services.DayPlanService
import java.time.LocalDate

class DayPlanActivityViewModel @AssistedInject constructor(
    private val dayPlanService: DayPlanService,
    @Assisted private val date: LocalDate
) : ViewModel() {

    private val _dayPlan = MutableLiveData<LoadingState<DayPlanResponse.DayPlan>>()

    val dayPlan: LiveData<LoadingState<DayPlanResponse.DayPlan>>
        get() = _dayPlan

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            dayPlanService.findByDate(date)
                .onEach { _dayPlan.value = it }
                .launchIn(viewModelScope)
        }
    }

    fun deleteRecipe(date: LocalDate, recipeId: Int): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            dayPlanService.deleteRecipe(date, recipeId)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    fun changeIngredientState(
        date: LocalDate,
        recipeId: Int,
        ingredientGroupId: Int,
        ingredientId: Int,
        isChecked: Boolean
    ): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            dayPlanService.changeIngredientState(date, recipeId, ingredientGroupId, ingredientId, isChecked)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }


    @AssistedFactory
    interface Factory {
        fun create(date: LocalDate): DayPlanActivityViewModel
    }

    companion object {
        fun provideFactory(factory: Factory, date: LocalDate): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return factory.create(date) as T
                }
            }
    }

}