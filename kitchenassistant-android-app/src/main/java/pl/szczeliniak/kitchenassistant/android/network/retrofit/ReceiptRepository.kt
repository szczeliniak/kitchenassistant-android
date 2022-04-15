package pl.szczeliniak.kitchenassistant.android.network.retrofit

import pl.szczeliniak.kitchenassistant.android.network.requests.*
import pl.szczeliniak.kitchenassistant.android.network.responses.CategoriesResponse
import pl.szczeliniak.kitchenassistant.android.network.responses.ReceiptResponse
import pl.szczeliniak.kitchenassistant.android.network.responses.ReceiptsResponse
import pl.szczeliniak.kitchenassistant.android.network.responses.SuccessResponse
import retrofit2.http.*

interface ReceiptRepository {

    @GET("/receipts")
    suspend fun findAll(
        @Query("userId") userId: Int?,
        @Query("categoryId") categoryId: Int?,
        @Query("name") name: String?
    ): ReceiptsResponse

    @GET("/receipts/{id}")
    suspend fun findById(@Path("id") receiptId: Int): ReceiptResponse

    @DELETE("/receipts/{id}")
    suspend fun delete(@Path("id") receiptId: Int): SuccessResponse

    @DELETE("/receipts/{id}/ingredients/{ingredientId}")
    suspend fun deleteIngredient(@Path("id") receiptId: Int, @Path("ingredientId") ingredientId: Int): SuccessResponse

    @DELETE("/receipts/{id}/steps/{stepId}")
    suspend fun deleteStep(@Path("id") receiptId: Int, @Path("stepId") stepId: Int): SuccessResponse

    @POST("/receipts")
    suspend fun add(@Body request: AddReceiptRequest): SuccessResponse

    @PUT("/receipts/{id}")
    suspend fun update(@Path("id") receiptId: Int, @Body request: UpdateReceiptRequest): SuccessResponse

    @POST("/receipts/{id}/ingredients")
    suspend fun addIngredient(@Path("id") receiptId: Int, @Body request: AddIngredientRequest): SuccessResponse

    @PUT("/receipts/{id}/ingredients/{ingredientId}")
    suspend fun updateIngredient(
        @Path("id") receiptId: Int,
        @Path("ingredientId") ingredientId: Int,
        @Body request: UpdateIngredientRequest
    ): SuccessResponse

    @POST("/receipts/{id}/steps")
    suspend fun addStep(@Path("id") receiptId: Int, @Body request: AddStepRequest): SuccessResponse

    @PUT("/receipts/{id}/steps/{stepId}")
    suspend fun updateStep(
        @Path("id") receiptId: Int,
        @Path("stepId") stepId: Int,
        @Body request: UpdateStepRequest
    ): SuccessResponse

    @GET("/receipts/categories")
    suspend fun findAllCategories(@Query("userId") userId: Int?): CategoriesResponse

    @DELETE("/receipts/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Int): SuccessResponse

    @POST("/receipts/categories")
    suspend fun addCategory(@Body request: AddCategoryRequest): SuccessResponse

    @PUT("/receipts/categories/{id}")
    suspend fun updateCategory(@Path("id") id: Int, @Body request: UpdateCategoryRequest): SuccessResponse

}