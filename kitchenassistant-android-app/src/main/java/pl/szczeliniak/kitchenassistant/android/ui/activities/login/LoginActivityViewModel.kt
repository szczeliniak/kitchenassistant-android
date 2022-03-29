package pl.szczeliniak.kitchenassistant.android.ui.activities.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.LoginRequest
import pl.szczeliniak.kitchenassistant.android.services.LoginService
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class LoginActivityViewModel @Inject constructor(
    private val loginService: LoginService,
) : ViewModel() {

    fun login(request: LoginRequest): LiveData<LoadingState<Boolean>> {
        val liveData = MutableLiveData<LoadingState<Boolean>>()
        viewModelScope.launch {
            loginService.login(request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

}