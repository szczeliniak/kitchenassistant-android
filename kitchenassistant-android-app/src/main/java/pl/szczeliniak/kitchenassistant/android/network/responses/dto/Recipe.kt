package pl.szczeliniak.kitchenassistant.android.network.responses.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Recipe(
    val id: Int,
    val name: String,
    val author: String?,
    val favorite: Boolean,
    val category: Category?,
    val tags: List<String>,
) : Parcelable