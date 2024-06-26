package pl.szczeliniak.cookbook.android.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.szczeliniak.cookbook.android.exceptions.CookBookNetworkException
import pl.szczeliniak.cookbook.android.network.LoadingState
import pl.szczeliniak.cookbook.android.network.requests.LoginRequest
import pl.szczeliniak.cookbook.android.network.requests.LoginWithFacebookRequest
import pl.szczeliniak.cookbook.android.network.responses.LoginResponse
import pl.szczeliniak.cookbook.android.network.retrofit.UserRepository
import retrofit2.HttpException

class UserService(private val repository: UserRepository) {

    suspend fun login(request: LoginRequest): Flow<LoadingState<LoginResponse>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.login(request)))
            } catch (e: CookBookNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: HttpException) {
                emit(LoadingState.HttpException(e))
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun loginWithFacebook(request: LoginWithFacebookRequest): Flow<LoadingState<LoginResponse>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.loginWithFacebook(request)))
            } catch (e: CookBookNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: HttpException) {
                emit(LoadingState.HttpException(e))
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun refreshToken(): Flow<LoadingState<LoginResponse>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.refreshToken()))
            } catch (e: CookBookNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: HttpException) {
                emit(LoadingState.HttpException(e))
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

}