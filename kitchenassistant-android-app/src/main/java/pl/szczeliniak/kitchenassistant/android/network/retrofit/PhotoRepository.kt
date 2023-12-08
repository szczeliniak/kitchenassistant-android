package pl.szczeliniak.kitchenassistant.android.network.retrofit

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import pl.szczeliniak.kitchenassistant.android.network.responses.*
import retrofit2.Response
import retrofit2.http.*

interface PhotoRepository {

    @Streaming
    @GET("/photos/{fileName}")
    suspend fun downloadPhoto(@Path("fileName") fileName: String): Response<ResponseBody>

    @Multipart
    @POST("/photos")
    suspend fun uploadPhoto(@Part file: MultipartBody.Part): UploadPhotoResponse

}