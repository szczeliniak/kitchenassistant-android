package pl.szczeliniak.kitchenassistant.android.network.requests

data class AddRecipeRequest(
    val name: String,
    val author: String?,
    val source: String?,
    val description: String?,
    val categoryId: Int?,
    val tags: List<String>,
    val photoName: String?
)