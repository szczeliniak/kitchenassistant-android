package pl.szczeliniak.kitchenassistant.android.ui.dialogs.choosedayplanforrecipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.responses.DayPlansResponse
import pl.szczeliniak.kitchenassistant.android.services.DayPlanService
import javax.inject.Inject

@HiltViewModel
class ChooseDayPlanForRecipeDialogViewModel @Inject constructor(
    private val dayPlanService: DayPlanService
) : ViewModel() {

    private val _dayPlans = MutableLiveData<LoadingState<DayPlansResponse>>()

    val dayPlans: LiveData<LoadingState<DayPlansResponse>>
        get() = _dayPlans

    init {
        reloadDayPlans()
    }

    private fun reloadDayPlans() {
        viewModelScope.launch {
            dayPlanService.findAll(false, null, 0, 20)
                .onEach { _dayPlans.value = it }
                .launchIn(viewModelScope)
        }
    }

}