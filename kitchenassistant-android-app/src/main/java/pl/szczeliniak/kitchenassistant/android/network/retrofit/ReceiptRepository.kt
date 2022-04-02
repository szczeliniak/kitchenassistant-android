package pl.szczeliniak.kitchenassistant.android.network.retrofit

import pl.szczeliniak.kitchenassistant.android.network.requests.AddReceiptRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateReceiptRequest
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

    @POST("/receipts")
    suspend fun add(@Body request: AddReceiptRequest): SuccessResponse

    @PUT("/receipts/{id}")
    suspend fun update(@Path("id") receiptId: Int, @Body request: UpdateReceiptRequest): SuccessResponse


}