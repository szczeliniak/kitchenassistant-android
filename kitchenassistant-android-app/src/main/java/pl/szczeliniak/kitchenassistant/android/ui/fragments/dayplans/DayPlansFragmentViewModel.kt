package pl.szczeliniak.kitchenassistant.android.ui.fragments.dayplans

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
class DayPlansFragmentViewModel @Inject constructor(
    private val dayPlansService: DayPlanService
) : ViewModel() {

    companion object {
        private const val LIMIT = 20
    }

    private val _dayPlans = MutableLiveData<LoadingState<DayPlansResponse>>()

    val dayPlans: LiveData<LoadingState<DayPlansResponse>> get() = _dayPlans

    init {
        reload(1)
    }

    fun reload(page: Int, name: String? = null) {
        viewModelScope.launch {
            dayPlansService.findAll(false, name, page, LIMIT)
                .onEach { _dayPlans.value = it }
                .launchIn(viewModelScope)
        }
    }

    fun delete(id: Int): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            dayPlansService.delete(id)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

}