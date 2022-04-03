package pl.szczeliniak.kitchenassistant.android.network.requests

import pl.szczeliniak.kitchenassistant.android.network.enums.IngredientUnit

data class AddIngredientRequest(
    val name: String,
    val quantity: String,
    val unit: IngredientUnit
)