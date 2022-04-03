package pl.szczeliniak.kitchenassistant.android.network.requests

data class AddShoppingListRequest(
    val title: String,
    val description: String?,
    val userId: Int
)