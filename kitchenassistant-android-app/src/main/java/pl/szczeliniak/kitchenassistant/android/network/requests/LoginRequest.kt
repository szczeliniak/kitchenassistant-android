package pl.szczeliniak.kitchenassistant.android.network.requests

data class LoginRequest(
    val email: String,
    val password: String
)