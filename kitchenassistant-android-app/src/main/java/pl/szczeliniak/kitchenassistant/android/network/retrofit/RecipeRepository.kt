package pl.szczeliniak.kitchenassistant.android.network.retrofit

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import pl.szczeliniak.kitchenassistant.android.network.requests.*
import pl.szczeliniak.kitchenassistant.android.network.responses.*
import retrofit2.Response
import retrofit2.http.*

interface RecipeRepository {

    @GET("/recipes")
    suspend fun findAll(
        @Query("userId") userId: Int?,
        @Query("categoryId") categoryId: Int?,
        @Query("name") name: String?,
        @Query("tag") tag: String?,
        @Query("onlyFavorites") onlyFavorites: Boolean,
        @Query("page") page: Int?,
        @Query("limit") limit: Int?
    ): RecipesResponse

    @GET("/recipes/{id}")
    suspend fun findById(@Path("id") recipeId: Int): RecipeResponse

    @DELETE("/recipes/{id}")
    suspend fun delete(@Path("id") recipeId: Int): SuccessResponse

    @POST("/recipes")
    suspend fun add(@Body request: AddRecipeRequest): SuccessResponse

    @PUT("/recipes/{id}")
    suspend fun update(@Path("id") recipeId: Int, @Body request: UpdateRecipeRequest): SuccessResponse

    @POST("/recipes/{id}/ingredientGroups")
    suspend fun addIngredientGroup(
        @Path("id") recipeId: Int,
        @Body request: AddIngredientGroupRequest
    ): SuccessResponse

    @PUT("/recipes/{id}/ingredientGroups/{ingredientGroupId}")
    suspend fun editIngredientGroup(
        @Path("id") recipeId: Int,
        @Path("ingredientGroupId") ingredientGroupId: Int,
        @Body request: EditIngredientGroupRequest
    ): SuccessResponse

    @DELETE("/recipes/{id}/ingredientGroups/{ingredientGroupId}/ingredients/{ingredientId}")
    suspend fun deleteIngredient(
        @Path("id") recipeId: Int,
        @Path("ingredientGroupId") ingredientGroupId: Int,
        @Path("ingredientId") ingredientId: Int
    ): SuccessResponse

    @POST("/recipes/{id}/steps")
    suspend fun addStep(@Path("id") recipeId: Int, @Body request: AddStepRequest): SuccessResponse

    @PUT("/recipes/{id}/steps/{stepId}")
    suspend fun updateStep(
        @Path("id") recipeId: Int,
        @Path("stepId") stepId: Int,
        @Body request: UpdateStepRequest
    ): SuccessResponse

    @DELETE("/recipes/{id}/steps/{stepId}")
    suspend fun deleteStep(@Path("id") recipeId: Int, @Path("stepId") stepId: Int): SuccessResponse

    @GET("/recipes/categories")
    suspend fun findAllCategories(@Query("userId") userId: Int?): CategoriesResponse

    @GET("/recipes/tags")
    suspend fun findAllTags(@Query("userId") userId: Int?): TagsResponse

    @GET("/recipes/authors")
    suspend fun findAllAuthors(@Query("userId") userId: Int?): AuthorsResponse

    @DELETE("/recipes/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Int): SuccessResponse

    @POST("/recipes/categories")
    suspend fun addCategory(@Body request: AddCategoryRequest): SuccessResponse

    @PUT("/recipes/categories/{id}")
    suspend fun updateCategory(@Path("id") id: Int, @Body request: UpdateCategoryRequest): SuccessResponse

    @PUT("/recipes/{id}/favorite/{isFavorite}")
    suspend fun setFavorite(@Path("id") id: Int, @Path("isFavorite") isFavorite: Boolean): SuccessResponse

    @Streaming
    @GET("/recipes/{id}/photo")
    suspend fun downloadPhoto(@Path("id") id: Int): Response<ResponseBody>

    @Multipart
    @POST("/recipes/photo")
    suspend fun uploadPhoto(@Part part: MultipartBody.Part): UploadPhotoResponse

    @GET("/recipes/{recipeId}/ingredientGroups/{ingredientGroupId}")
    suspend fun getIngredientGroupById(
        @Path("recipeId") recipeId: Int,
        @Path("ingredientGroupId") ingredientGroupId: Int
    ): IngredientGroupResponse

}