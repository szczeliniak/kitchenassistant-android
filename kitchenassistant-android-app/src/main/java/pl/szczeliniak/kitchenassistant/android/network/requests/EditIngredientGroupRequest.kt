package pl.szczeliniak.kitchenassistant.android.network.requests

data class EditIngredientGroupRequest(
    val name: String,
    val ingredients: List<EditIngredientDto>
)