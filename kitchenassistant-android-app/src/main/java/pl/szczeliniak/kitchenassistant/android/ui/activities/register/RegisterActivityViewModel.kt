package pl.szczeliniak.kitchenassistant.android.ui.activities.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.RegisterRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.LoginResponse
import pl.szczeliniak.kitchenassistant.android.services.RegisterService
import javax.inject.Inject

@HiltViewModel
class RegisterActivityViewModel @Inject constructor(
    private val registerService: RegisterService,
) : ViewModel() {

    fun login(request: RegisterRequest): LiveData<LoadingState<LoginResponse>> {
        val liveData = MutableLiveData<LoadingState<LoginResponse>>()
        viewModelScope.launch {
            registerService.register(request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

}