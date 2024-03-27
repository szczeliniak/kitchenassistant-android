package pl.szczeliniak.kitchenassistant.android.network.retrofit

import pl.szczeliniak.kitchenassistant.android.network.requests.LoginRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.LoginWithFacebookRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.LoginResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface UserRepository {

    @Headers("No-Authentication: true", "Content-Type: application/json")
    @POST("/users/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @Headers("No-Authentication: true", "Content-Type: application/json")
    @POST("/users/login/facebook")
    suspend fun loginWithFacebook(@Body request: LoginWithFacebookRequest): LoginResponse

    @Headers("Token-Type: REFRESH", "Content-Type: application/json")
    @POST("/users/refresh")
    suspend fun refreshToken(): LoginResponse

}