package pl.szczeliniak.kitchenassistant.android.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.szczeliniak.kitchenassistant.android.exceptions.KitchenAssistantNetworkException
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.AddIngredientRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.AddReceiptRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.AddStepRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Receipt
import pl.szczeliniak.kitchenassistant.android.network.retrofit.ReceiptRepository

class ReceiptService constructor(private val repository: ReceiptRepository) {

    suspend fun findAll(): Flow<LoadingState<List<Receipt>>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.findAll().receipts))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun findById(receiptId: Int): Flow<LoadingState<Receipt>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.findById(receiptId).receipt))
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

    suspend fun deleteIngredient(receiptId: Int, ingredientId: Int): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.deleteIngredient(receiptId, ingredientId).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun deleteStep(receiptId: Int, stepId: Int): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.deleteStep(receiptId, stepId).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun add(request: AddReceiptRequest): Flow<LoadingState<Int>> {
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

    suspend fun addIngredient(receiptId: Int, request: AddIngredientRequest): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.addIngredient(receiptId, request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun addStep(receiptId: Int, request: AddStepRequest): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.addStep(receiptId, request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

}