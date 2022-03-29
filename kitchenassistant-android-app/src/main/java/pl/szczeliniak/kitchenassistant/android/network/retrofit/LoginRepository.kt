package pl.szczeliniak.kitchenassistant.android.network.retrofit

import pl.szczeliniak.kitchenassistant.android.network.requests.LoginRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.LoginResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface LoginRepository {

    @Headers("No-Authentication: true", "Content-Type: application/json")
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

}