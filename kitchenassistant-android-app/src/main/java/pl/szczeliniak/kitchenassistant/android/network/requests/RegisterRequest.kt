package pl.szczeliniak.kitchenassistant.android.network.requests

data class RegisterRequest(
    val email: String,
    val password: String
)