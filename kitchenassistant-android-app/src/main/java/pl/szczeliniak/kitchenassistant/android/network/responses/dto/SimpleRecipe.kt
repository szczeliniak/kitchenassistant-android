package pl.szczeliniak.kitchenassistant.android.network.responses.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SimpleRecipe(
    val id: Int,
    val name: String
) : Parcelable