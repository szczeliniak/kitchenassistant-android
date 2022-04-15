package pl.szczeliniak.kitchenassistant.android.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.szczeliniak.kitchenassistant.android.exceptions.KitchenAssistantNetworkException
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.AddShoppingListItemRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.AddShoppingListRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateShoppingListItemRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateShoppingListRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ShoppingList
import pl.szczeliniak.kitchenassistant.android.network.retrofit.ShoppingListRepository
import java.time.LocalDate

class ShoppingListService constructor(
    private val repository: ShoppingListRepository,
    private val localStorageService: LocalStorageService
) {

    suspend fun findAll(archived: Boolean, name: String?, date: LocalDate?): Flow<LoadingState<List<ShoppingList>>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(
                    LoadingState.Success(
                        repository.findAll(
                            localStorageService.getId(),
                            archived,
                            name,
                            date
                        ).shoppingLists
                    )
                )
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun findById(shoppingListId: Int): Flow<LoadingState<ShoppingList>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.findById(shoppingListId).shoppingList))
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

    suspend fun changeItemState(shoppingListId: Int, shoppingListItemId: Int, state: Boolean): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.changeItemState(shoppingListId, shoppingListItemId, state).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun archive(id: Int, isArchived: Boolean): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.archive(id, isArchived).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun deleteShoppingListItem(shoppingListId: Int, shoppingListItemId: Int): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.deleteShoppingListItem(shoppingListId, shoppingListItemId).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun add(request: AddShoppingListRequest): Flow<LoadingState<Int>> {
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

    suspend fun update(shoppingListId: Int, request: UpdateShoppingListRequest): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.update(shoppingListId, request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun addShoppingListItem(shoppingListId: Int, request: AddShoppingListItemRequest): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(repository.addShoppingListItem(shoppingListId, request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun updateShoppingListItem(
        shoppingListId: Int,
        shoppingListItemId: Int,
        request: UpdateShoppingListItemRequest
    ): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(
                    LoadingState.Success(
                        repository.updateShoppingListItem(
                            shoppingListId,
                            shoppingListItemId,
                            request
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