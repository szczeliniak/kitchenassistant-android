package pl.szczeliniak.kitchenassistant.android.network.requests

data class AddStepRequest(
    val description: String,
    val photoName: String?,
    val sequence: Int?
)