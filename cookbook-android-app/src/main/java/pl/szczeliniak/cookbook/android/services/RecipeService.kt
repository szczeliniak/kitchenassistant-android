package pl.szczeliniak.cookbook.android.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.szczeliniak.cookbook.android.exceptions.CookBookNetworkException
import pl.szczeliniak.cookbook.android.network.LoadingState
import pl.szczeliniak.cookbook.android.network.responses.CategoriesResponse
import pl.szczeliniak.cookbook.android.network.responses.RecipeResponse
import pl.szczeliniak.cookbook.android.network.responses.RecipesResponse
import pl.szczeliniak.cookbook.android.network.retrofit.RecipeRepository

class RecipeService(
    private val recipeRepository: RecipeRepository
) {

    suspend fun findAll(
        categoryId: Int? = null,
        search: String? = null,
        page: Long? = null,
        limit: Int? = null
    ): Flow<LoadingState<RecipesResponse>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(
                    LoadingState.Success(
                        recipeRepository.findAll(categoryId, search, page, limit)
                    )
                )
            } catch (e: CookBookNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun findById(recipeId: Int): Flow<LoadingState<RecipeResponse.Recipe>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(recipeRepository.findById(recipeId).recipe))
            } catch (e: CookBookNetworkException) {
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
            } catch (e: CookBookNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun findAllCategories(): Flow<LoadingState<List<CategoriesResponse.Category>>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(recipeRepository.findAllCategories().categories))
            } catch (e: CookBookNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

}