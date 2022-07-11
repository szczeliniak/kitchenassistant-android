package pl.szczeliniak.kitchenassistant.android.network.responses.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DayPlanIngredient(
    val name: String,
    val quantity: String?,
) : Parcelable