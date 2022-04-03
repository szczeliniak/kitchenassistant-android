package pl.szczeliniak.kitchenassistant.android.network.requests

data class AddShoppingListRequest(
    val name: String,
    val description: String?,
    val userId: Int
)