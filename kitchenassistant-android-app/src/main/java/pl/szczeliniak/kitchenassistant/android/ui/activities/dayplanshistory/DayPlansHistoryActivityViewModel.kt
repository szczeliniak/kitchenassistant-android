package pl.szczeliniak.kitchenassistant.android.ui.activities.dayplanshistory

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
import pl.szczeliniak.kitchenassistant.android.network.retrofit.DayPlanRepository
import pl.szczeliniak.kitchenassistant.android.services.DayPlanService
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DayPlansHistoryActivityViewModel @Inject constructor(
    private val dayPlanService: DayPlanService
) : ViewModel() {

    companion object {
        private const val LIMIT = 20
    }

    private val _dayPlans = MutableLiveData<LoadingState<DayPlansResponse>>()

    val dayPlans: LiveData<LoadingState<DayPlansResponse>> get() = _dayPlans

    init {
        reloadDayPlans(1)
    }

    fun reloadDayPlans(page: Int) {
        viewModelScope.launch {
            dayPlanService.findAll(page, LIMIT, to = LocalDate.now().minusDays(1), sort = DayPlanRepository.Sort.DESC)
                .onEach { _dayPlans.value = it }
                .launchIn(viewModelScope)
        }
    }

    fun delete(dayPlanId: Int): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            dayPlanService.delete(dayPlanId)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

}