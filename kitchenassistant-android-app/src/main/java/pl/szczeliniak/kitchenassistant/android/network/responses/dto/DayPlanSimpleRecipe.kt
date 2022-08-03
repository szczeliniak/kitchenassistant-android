package pl.szczeliniak.kitchenassistant.android.network.responses.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DayPlanSimpleRecipe(
    val id: Int,
    val name: String,
    val author: String?,
    val category: String?
) : Parcelable