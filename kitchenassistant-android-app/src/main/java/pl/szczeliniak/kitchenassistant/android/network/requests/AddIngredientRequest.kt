package pl.szczeliniak.kitchenassistant.android.network.requests

data class AddIngredientRequest(
    val name: String,
    val quantity: String?
)