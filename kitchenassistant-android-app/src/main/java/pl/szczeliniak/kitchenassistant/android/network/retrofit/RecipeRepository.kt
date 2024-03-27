package pl.szczeliniak.kitchenassistant.android.network.retrofit

import pl.szczeliniak.kitchenassistant.android.network.responses.*
import retrofit2.http.*

interface RecipeRepository {

    @GET("/recipes")
    suspend fun findAll(
        @Query("categoryId") categoryId: Int?,
        @Query("search") search: String?,
        @Query("page") page: Long?,
        @Query("limit") limit: Int?
    ): RecipesResponse

    @GET("/recipes/{id}")
    suspend fun findById(@Path("id") recipeId: Int): RecipeResponse

    @DELETE("/recipes/{id}")
    suspend fun archive(@Path("id") recipeId: Int): SuccessResponse

    @GET("/recipes/categories")
    suspend fun findAllCategories(): CategoriesResponse

    @GET("/recipes/authors")
    suspend fun findAllAuthors(): AuthorsResponse

    @PUT("/recipes/{id}/favorite/{isFavorite}")
    suspend fun setFavorite(@Path("id") id: Int, @Path("isFavorite") isFavorite: Boolean): SuccessResponse

}