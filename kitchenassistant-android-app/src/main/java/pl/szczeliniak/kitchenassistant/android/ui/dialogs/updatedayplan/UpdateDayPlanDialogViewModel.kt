package pl.szczeliniak.kitchenassistant.android.ui.dialogs.updatedayplan

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateDayPlanRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.DayPlanResponse
import pl.szczeliniak.kitchenassistant.android.services.DayPlanService
import java.time.LocalDate

class UpdateDayPlanDialogViewModel @AssistedInject constructor(
    private val dayPlanService: DayPlanService,
    @Assisted private val date: LocalDate
) : ViewModel() {

    private val _dayPlan = MutableLiveData<LoadingState<DayPlanResponse.DayPlan>>()

    val dayPlan: LiveData<LoadingState<DayPlanResponse.DayPlan>>
        get() = _dayPlan

    init {
        reload()
    }

    private fun reload() {
        viewModelScope.launch {
            dayPlanService.findById(date)
                .onEach { _dayPlan.value = it }
                .launchIn(viewModelScope)
        }
    }

    fun update(date: LocalDate, request: UpdateDayPlanRequest): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            dayPlanService.update(date, request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    @AssistedFactory
    interface Factory {
        fun create(date: LocalDate): UpdateDayPlanDialogViewModel
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