package pl.szczeliniak.kitchenassistant.android.network.requests

data class EditIngredientDto(
    val ingredientId: Int?,
    val name: String,
    val quantity: String?
)