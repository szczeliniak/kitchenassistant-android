package pl.szczeliniak.kitchenassistant.android.network.requests

data class UpdateStepRequest(
    val name: String,
    val description: String?,
    val sequence: Int?
)