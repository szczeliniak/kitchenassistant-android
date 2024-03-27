package pl.szczeliniak.kitchenassistant.android.network.responses

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String
)