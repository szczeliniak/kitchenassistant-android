package pl.szczeliniak.kitchenassistant.android.ui.activities.dayplan

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.DayPlanDetails
import pl.szczeliniak.kitchenassistant.android.services.DayPlanService

class DayPlanActivityViewModel @AssistedInject constructor(
    private val dayPlanService: DayPlanService,
    @Assisted private val dayPlanId: Int
) : ViewModel() {

    private val _dayPlan = MutableLiveData<LoadingState<DayPlanDetails>>()

    val dayPlan: LiveData<LoadingState<DayPlanDetails>>
        get() = _dayPlan

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            dayPlanService.findById(dayPlanId)
                .onEach { _dayPlan.value = it }
                .launchIn(viewModelScope)
        }
    }

    fun deleteRecipe(dayPlanId: Int, recipeId: Int): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            dayPlanService.unassignRecipe(dayPlanId, recipeId)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    @AssistedFactory
    interface Factory {
        fun create(dayPlanId: Int): DayPlanActivityViewModel
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