package pl.szczeliniak.kitchenassistant.android.ui.dialogs.updatedayplan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateDayPlanRequest
import pl.szczeliniak.kitchenassistant.android.services.DayPlanService
import javax.inject.Inject

@HiltViewModel
class UpdateDayPlanDialogViewModel @Inject constructor(
    private val dayPlanService: DayPlanService,
) : ViewModel() {

    fun update(dayPlanId: Int, request: UpdateDayPlanRequest): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            dayPlanService.update(dayPlanId, request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

}