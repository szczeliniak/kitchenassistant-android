package pl.szczeliniak.kitchenassistant.android.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.szczeliniak.kitchenassistant.android.exceptions.KitchenAssistantNetworkException
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.LoginRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.LoginResponse
import pl.szczeliniak.kitchenassistant.android.network.retrofit.LoginRepository
import retrofit2.HttpException

class LoginService constructor(private val repository: LoginRepository) {

    suspend fun login(request: LoginRequest): Flow<LoadingState<LoginResponse>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.login(request)))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: HttpException) {
                emit(LoadingState.HttpException(e))
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

}