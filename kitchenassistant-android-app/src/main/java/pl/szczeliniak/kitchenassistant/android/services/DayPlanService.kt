package pl.szczeliniak.kitchenassistant.android.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.szczeliniak.kitchenassistant.android.exceptions.KitchenAssistantNetworkException
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.AddRecipeToDayPlanRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.DayPlanResponse
import pl.szczeliniak.kitchenassistant.android.network.responses.DayPlansResponse
import pl.szczeliniak.kitchenassistant.android.network.retrofit.DayPlanRepository
import java.time.LocalDate

class DayPlanService(
    private val repository: DayPlanRepository
) {

    suspend fun findAll(
        page: Long? = null,
        limit: Int? = null,
        since: LocalDate? = null,
        to: LocalDate? = null,
        sort: DayPlanRepository.Sort? = null
    ): Flow<LoadingState<DayPlansResponse>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(
                    LoadingState.Success(
                        repository.findAll(
                            page, limit, since, to, sort
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

    suspend fun deleteRecipe(date: LocalDate, recipeId: Int): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.deleteRecipe(date, recipeId).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun findByDate(date: LocalDate): Flow<LoadingState<DayPlanResponse.DayPlan>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.findByDate(date).dayPlan))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun addRecipe(request: AddRecipeToDayPlanRequest): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.addRecipe(request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun changeIngredientState(
        date: LocalDate, recipeId: Int, ingredientGroupId: Int, ingredientId: Int, isChecked: Boolean
    ): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(
                    LoadingState.Success(
                        repository.changeIngredientState(
                            date, recipeId, ingredientGroupId, ingredientId, isChecked
                        ).id
                    )
                )
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

}