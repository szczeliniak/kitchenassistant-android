package pl.szczeliniak.kitchenassistant.android.network.retrofit

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import pl.szczeliniak.kitchenassistant.android.network.responses.SuccessResponse
import retrofit2.Response
import retrofit2.http.*

interface FileRepository {

    @Streaming
    @GET("/files/{id}")
    suspend fun download(@Path("id") id: Int): Response<ResponseBody>

    @Multipart
    @POST("/files")
    suspend fun upload(@Query("userId") userId: Int, @Part part: MultipartBody.Part): SuccessResponse

}