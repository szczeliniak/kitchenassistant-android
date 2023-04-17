package pl.szczeliniak.kitchenassistant.android.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.szczeliniak.kitchenassistant.android.exceptions.KitchenAssistantNetworkException
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.*
import pl.szczeliniak.kitchenassistant.android.network.responses.RecipesResponse
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Category
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.IngredientGroup
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.RecipeDetails
import pl.szczeliniak.kitchenassistant.android.network.retrofit.RecipeRepository

class RecipeService(
    private val recipeRepository: RecipeRepository,
    private val localStorageService: LocalStorageService
) {

    suspend fun findAll(
        categoryId: Int? = null,
        recipeName: String? = null,
        tag: String? = null,
        onlyFavorites: Boolean = false,
        page: Int? = null,
        limit: Int? = null
    ): Flow<LoadingState<RecipesResponse>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(
                    LoadingState.Success(
                        recipeRepository.findAll(
                            localStorageService.getId(),
                            categoryId,
                            recipeName,
                            tag, onlyFavorites, page, limit
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

    suspend fun findById(recipeId: Int): Flow<LoadingState<RecipeDetails>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(recipeRepository.findById(recipeId).recipe))
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
                emit(LoadingState.Success(recipeRepository.delete(id).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun deleteIngredient(recipeId: Int, ingredientGroupId: Int, ingredientId: Int): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(
                    LoadingState.Success(
                        recipeRepository.deleteIngredient(
                            recipeId,
                            ingredientGroupId,
                            ingredientId
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

    suspend fun deleteStep(recipeId: Int, stepId: Int): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(recipeRepository.deleteStep(recipeId, stepId).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun add(request: AddRecipeRequest): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(recipeRepository.add(request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun update(recipeId: Int, request: UpdateRecipeRequest): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(recipeRepository.update(recipeId, request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun addIngredientGroup(
        recipeId: Int,
        request: AddIngredientGroupRequest
    ): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(recipeRepository.addIngredientGroup(recipeId, request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun editIngredientGroup(
        recipeId: Int,
        ingredientGroupId: Int,
        request: EditIngredientGroupRequest
    ): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(
                    LoadingState.Success(
                        recipeRepository.editIngredientGroup(
                            recipeId,
                            ingredientGroupId,
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

    suspend fun addStep(recipeId: Int, request: AddStepRequest): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(recipeRepository.addStep(recipeId, request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun updateStep(recipeId: Int, stepId: Int, request: UpdateStepRequest): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(recipeRepository.updateStep(recipeId, stepId, request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun findAllCategories(): Flow<LoadingState<List<Category>>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(recipeRepository.findAllCategories(localStorageService.getId()).categories))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun findAllTags(): Flow<LoadingState<List<String>>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(recipeRepository.findAllTags(localStorageService.getId()).tags))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun findAllAuthors(): Flow<LoadingState<List<String>>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(recipeRepository.findAllAuthors(localStorageService.getId()).authors))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun deleteCategory(id: Int): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(recipeRepository.deleteCategory(id).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun addCategory(request: AddCategoryRequest): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(recipeRepository.addCategory(request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun updateCategory(categoryId: Int, request: UpdateCategoryRequest): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(recipeRepository.updateCategory(categoryId, request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    fun setFavorite(id: Int, isFavorite: Boolean): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(recipeRepository.setFavorite(id, isFavorite).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    fun getIngredientGroupById(recipeId: Int, ingredientGroupId: Int): Flow<LoadingState<IngredientGroup>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(
                    LoadingState.Success(
                        recipeRepository.getIngredientGroupById(
                            recipeId,
                            ingredientGroupId
                        ).ingredientGroup
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