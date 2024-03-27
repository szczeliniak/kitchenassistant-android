package pl.szczeliniak.kitchenassistant.android.ui.activities.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.LoginRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.LoginWithFacebookRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.LoginResponse
import pl.szczeliniak.kitchenassistant.android.services.UserService
import javax.inject.Inject

@HiltViewModel
class LoginActivityViewModel @Inject constructor(
    private val userService: UserService,
) : ViewModel() {

    fun login(request: LoginRequest): LiveData<LoadingState<LoginResponse>> {
        val liveData = MutableLiveData<LoadingState<LoginResponse>>()
        viewModelScope.launch {
            userService.login(request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    fun loginWithFacebook(request: LoginWithFacebookRequest): LiveData<LoadingState<LoginResponse>> {
        val liveData = MutableLiveData<LoadingState<LoginResponse>>()
        viewModelScope.launch {
            userService.loginWithFacebook(request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    fun refreshToken(): LiveData<LoadingState<LoginResponse>> {
        val liveData = MutableLiveData<LoadingState<LoginResponse>>()
        viewModelScope.launch {
            userService.refreshToken()
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

}