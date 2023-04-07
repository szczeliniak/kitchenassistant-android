package pl.szczeliniak.kitchenassistant.android.network.requests

data class UpdateRecipeRequest(
    val name: String,
    val author: String?,
    val source: String?,
    val description: String?,
    val categoryId: Int?,
    val tags: List<String>
)