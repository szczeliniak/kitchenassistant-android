package pl.szczeliniak.kitchenassistant.android.network.retrofit

import pl.szczeliniak.kitchenassistant.android.network.requests.AddIngredientRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.AddReceiptRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.AddStepRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.ReceiptResponse
import pl.szczeliniak.kitchenassistant.android.network.responses.ReceiptsResponse
import pl.szczeliniak.kitchenassistant.android.network.responses.SuccessResponse
import retrofit2.http.*

interface ReceiptRepository {

    @GET("/receipts")
    suspend fun findAll(): ReceiptsResponse

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

    @POST("/receipts/{id}/ingredients")
    suspend fun addIngredient(@Path("id") receiptId: Int, @Body request: AddIngredientRequest): SuccessResponse

    @POST("/receipts/{id}/steps")
    suspend fun addStep(@Path("id") receiptId: Int, @Body request: AddStepRequest): SuccessResponse

}