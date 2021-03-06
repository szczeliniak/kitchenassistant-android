package pl.szczeliniak.kitchenassistant.android.network.retrofit

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import pl.szczeliniak.kitchenassistant.android.network.requests.*
import pl.szczeliniak.kitchenassistant.android.network.responses.*
import retrofit2.Response
import retrofit2.http.*

interface ReceiptRepository {

    @GET("/receipts")
    suspend fun findAll(
        @Query("userId") userId: Int?,
        @Query("categoryId") categoryId: Int?,
        @Query("name") name: String?,
        @Query("tag") tag: String?,
        @Query("page") page: Int?,
        @Query("limit") limit: Int?
    ): ReceiptsResponse

    @GET("/receipts/{id}")
    suspend fun findById(@Path("id") receiptId: Int): ReceiptResponse

    @DELETE("/receipts/{id}")
    suspend fun delete(@Path("id") receiptId: Int): SuccessResponse

    @POST("/receipts")
    suspend fun add(@Body request: AddReceiptRequest): SuccessResponse

    @PUT("/receipts/{id}")
    suspend fun update(@Path("id") receiptId: Int, @Body request: UpdateReceiptRequest): SuccessResponse

    @POST("/receipts/{id}/ingredientGroups")
    suspend fun addIngredientGroup(
        @Path("id") receiptId: Int,
        @Body request: AddIngredientGroupRequest
    ): SuccessResponse

    @POST("/receipts/{id}/ingredientGroups/{ingredientGroupId}/ingredients")
    suspend fun addIngredient(
        @Path("id") receiptId: Int,
        @Path("ingredientGroupId") ingredientGroupId: Int,
        @Body request: AddIngredientRequest
    ): SuccessResponse

    @PUT("/receipts/{id}/ingredientGroups/{ingredientGroupId}/ingredients/{ingredientId}")
    suspend fun updateIngredient(
        @Path("id") receiptId: Int,
        @Path("ingredientGroupId") ingredientGroupId: Int,
        @Path("ingredientId") ingredientId: Int,
        @Body request: UpdateIngredientRequest
    ): SuccessResponse

    @DELETE("/receipts/{id}/ingredientGroups/{ingredientGroupId}/ingredients/{ingredientId}")
    suspend fun deleteIngredient(
        @Path("id") receiptId: Int,
        @Path("ingredientGroupId") ingredientGroupId: Int,
        @Path("ingredientId") ingredientId: Int
    ): SuccessResponse

    @POST("/receipts/{id}/steps")
    suspend fun addStep(@Path("id") receiptId: Int, @Body request: AddStepRequest): SuccessResponse

    @PUT("/receipts/{id}/steps/{stepId}")
    suspend fun updateStep(
        @Path("id") receiptId: Int,
        @Path("stepId") stepId: Int,
        @Body request: UpdateStepRequest
    ): SuccessResponse

    @DELETE("/receipts/{id}/steps/{stepId}")
    suspend fun deleteStep(@Path("id") receiptId: Int, @Path("stepId") stepId: Int): SuccessResponse

    @GET("/receipts/categories")
    suspend fun findAllCategories(@Query("userId") userId: Int?): CategoriesResponse

    @GET("/receipts/tags")
    suspend fun findAllTags(@Query("userId") userId: Int?): TagsResponse

    @GET("/receipts/authors")
    suspend fun findAllAuthors(@Query("userId") userId: Int?): AuthorsResponse

    @DELETE("/receipts/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Int): SuccessResponse

    @POST("/receipts/categories")
    suspend fun addCategory(@Body request: AddCategoryRequest): SuccessResponse

    @PUT("/receipts/categories/{id}")
    suspend fun updateCategory(@Path("id") id: Int, @Body request: UpdateCategoryRequest): SuccessResponse

    @PUT("/receipts/{id}/favorite/{isFavorite}")
    suspend fun setFavorite(@Path("id") id: Int, @Path("isFavorite") isFavorite: Boolean): SuccessResponse

    @Streaming
    @GET("/receipts/photos/{id}")
    suspend fun downloadPhoto(@Path("id") id: Int): Response<ResponseBody>

    @Multipart
    @POST("/receipts/photos")
    suspend fun uploadPhoto(@Query("userId") userId: Int, @Part part: MultipartBody.Part): SuccessResponse

}