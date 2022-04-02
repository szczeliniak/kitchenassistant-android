package pl.szczeliniak.kitchenassistant.android.network.requests.dto

import pl.szczeliniak.kitchenassistant.android.network.enums.IngredientUnit

data class NewIngredient(
    val name: String,
    val quantity: String,
    val unit: IngredientUnit
)