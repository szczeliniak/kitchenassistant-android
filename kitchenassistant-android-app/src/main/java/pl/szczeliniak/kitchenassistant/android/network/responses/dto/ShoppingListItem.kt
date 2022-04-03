package pl.szczeliniak.kitchenassistant.android.network.responses.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShoppingListItem(
    val id: Int,
    val name: String,
    val quantity: String,
    val sequence: String
) : Parcelable