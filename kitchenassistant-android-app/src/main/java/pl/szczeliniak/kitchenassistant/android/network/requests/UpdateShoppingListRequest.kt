package pl.szczeliniak.kitchenassistant.android.network.requests

data class UpdateShoppingListRequest(
    val name: String,
    val description: String?
)