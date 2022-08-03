package pl.szczeliniak.kitchenassistant.android.network.responses.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DayPlanRecipe(
    val name: String,
    val ingredientGroups: List<DayPlanIngredientGroup>,
    val author: String?
) : Parcelable