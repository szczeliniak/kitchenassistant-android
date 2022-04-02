package pl.szczeliniak.kitchenassistant.android.network.responses.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import pl.szczeliniak.kitchenassistant.android.network.enums.IngredientUnit

@Parcelize
data class Ingredient(
    val id: Int,
    val name: String,
    val quantity: String,
    val unit: IngredientUnit
) : Parcelable