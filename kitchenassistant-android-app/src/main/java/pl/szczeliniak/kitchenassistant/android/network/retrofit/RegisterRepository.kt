package pl.szczeliniak.kitchenassistant.android.network.retrofit

import pl.szczeliniak.kitchenassistant.android.network.requests.RegisterRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.LoginResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface RegisterRepository {

    @Headers("No-Authentication: true", "Content-Type: application/json")
    @POST("/register")
    suspend fun register(@Body request: RegisterRequest): LoginResponse

}