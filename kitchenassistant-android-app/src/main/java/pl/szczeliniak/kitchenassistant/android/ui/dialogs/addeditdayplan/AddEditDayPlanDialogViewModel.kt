package pl.szczeliniak.kitchenassistant.android.ui.dialogs.addeditdayplan

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.AddDayPlanRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateDayPlanRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.DayPlanDetails
import pl.szczeliniak.kitchenassistant.android.services.DayPlanService

class AddEditDayPlanDialogViewModel @AssistedInject constructor(
    private val dayPlanService: DayPlanService,
    @Assisted private val dayPlanId: Int?
) : ViewModel() {

    private val _dayPlan = MutableLiveData<LoadingState<DayPlanDetails>>()

    val dayPlan: LiveData<LoadingState<DayPlanDetails>>
        get() = _dayPlan

    init {
        if (dayPlanId != null) {
            viewModelScope.launch {
                dayPlanService.findById(dayPlanId)
                    .onEach { _dayPlan.value = it }
                    .launchIn(viewModelScope)
            }
        }
    }

    fun add(request: AddDayPlanRequest): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            dayPlanService.add(request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    fun update(dayPlanId: Int, request: UpdateDayPlanRequest): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            dayPlanService.update(dayPlanId, request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    @AssistedFactory
    interface Factory {
        fun create(dayPlanId: Int?): AddEditDayPlanDialogViewModel
    }

    companion object {
        fun provideFactory(factory: Factory, dayPlanId: Int?): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return factory.create(dayPlanId) as T
                }
            }
    }

}