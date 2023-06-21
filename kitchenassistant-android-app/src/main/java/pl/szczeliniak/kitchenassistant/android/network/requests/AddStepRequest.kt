package pl.szczeliniak.kitchenassistant.android.network.requests

data class AddStepRequest(
    val description: String,
    val sequence: Int?
)