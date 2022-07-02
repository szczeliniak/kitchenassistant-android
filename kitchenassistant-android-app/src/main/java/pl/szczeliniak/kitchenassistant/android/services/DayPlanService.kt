package pl.szczeliniak.kitchenassistant.android.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.szczeliniak.kitchenassistant.android.exceptions.KitchenAssistantNetworkException
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.AddDayPlanRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateDayPlanRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.DayPlansResponse
import pl.szczeliniak.kitchenassistant.android.network.retrofit.DayPlanRepository
import java.time.LocalDate

class DayPlanService constructor(
    private val repository: DayPlanRepository,
    private val localStorageService: LocalStorageService
) {

    suspend fun findAll(
        archived: Boolean,
        date: LocalDate?,
        page: Int?,
        limit: Int?
    ): Flow<LoadingState<DayPlansResponse>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(
                    LoadingState.Success(
                        repository.findAll(
                            localStorageService.getId(),
                            archived,
                            date,
                            page,
                            limit
                        )
                    )
                )
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun delete(id: Int): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.delete(id).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun add(request: AddDayPlanRequest): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.add(request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun update(dayPlanId: Int, request: UpdateDayPlanRequest): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.update(dayPlanId, request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

}