package pl.szczeliniak.cookbook.android.network.responses

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String
)