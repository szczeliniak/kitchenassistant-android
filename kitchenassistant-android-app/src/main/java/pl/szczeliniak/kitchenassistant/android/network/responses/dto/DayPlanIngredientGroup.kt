package pl.szczeliniak.kitchenassistant.android.network.responses.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DayPlanIngredientGroup(
    val name: String,
    val ingredients: List<DayPlanIngredient>
) : Parcelable