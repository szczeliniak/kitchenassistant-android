package pl.szczeliniak.kitchenassistant.android.network.requests

data class UpdateCategoryRequest(
    val name: String,
    val sequence: Int?
)