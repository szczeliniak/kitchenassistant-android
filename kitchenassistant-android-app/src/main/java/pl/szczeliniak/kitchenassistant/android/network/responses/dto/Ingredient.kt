package pl.szczeliniak.kitchenassistant.android.network.responses.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Ingredient(
    val id: Int,
    val name: String,
    val quantity: String,
    val unit: IngredientUnit
) : Parcelable {

    enum class IngredientUnit {
        GRAMS, KILOGRAMS, CUPS, TEE_SPOON, TABLE_SPOON, PINCH_OF
    }

}