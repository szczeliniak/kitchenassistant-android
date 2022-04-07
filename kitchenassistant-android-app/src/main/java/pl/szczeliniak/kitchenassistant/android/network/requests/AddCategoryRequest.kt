package pl.szczeliniak.kitchenassistant.android.network.requests

data class AddCategoryRequest(
    val name: String,
    val userId: Int
)