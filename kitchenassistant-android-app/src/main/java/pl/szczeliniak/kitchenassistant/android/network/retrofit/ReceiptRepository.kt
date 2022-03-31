package pl.szczeliniak.kitchenassistant.android.network.retrofit

import pl.szczeliniak.kitchenassistant.android.network.responses.ReceiptsResponse
import pl.szczeliniak.kitchenassistant.android.network.responses.SuccessResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface ReceiptRepository {

    @GET("/receipts")
    suspend fun findAll(): ReceiptsResponse

    @DELETE("/receipts/{id}")
    suspend fun delete(@Path("id") receiptId: Int): SuccessResponse

}