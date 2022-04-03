package pl.szczeliniak.kitchenassistant.android.network.responses.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShoppingList(
    val id: Int,
    val title: String,
    val description: String?,
    val items: List<ShoppingListItem>
) : Parcelable