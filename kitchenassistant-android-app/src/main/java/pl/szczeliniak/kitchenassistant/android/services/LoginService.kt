package pl.szczeliniak.kitchenassistant.android.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.szczeliniak.kitchenassistant.android.exceptions.ReceiptsStorageNetworkException
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.LoginRequest
import pl.szczeliniak.kitchenassistant.android.network.retrofit.LoginRepository
import retrofit2.HttpException

class LoginService constructor(
    private val repository: LoginRepository,
    private val localStorageService: LocalStorageService
) {

    suspend fun login(request: LoginRequest): Flow<LoadingState<Boolean>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                val response = repository.login(request)
                localStorageService.save(response.token)
                emit(LoadingState.Success(true))
            } catch (e: ReceiptsStorageNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: HttpException) {
                emit(LoadingState.HttpException(e))
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

}