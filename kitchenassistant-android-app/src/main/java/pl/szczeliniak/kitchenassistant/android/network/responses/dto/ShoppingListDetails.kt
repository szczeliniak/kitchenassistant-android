package pl.szczeliniak.kitchenassistant.android.network.responses.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class ShoppingListDetails(
    val id: Int,
    val name: String,
    val description: String?,
    val date: LocalDate?,
    val items: List<ShoppingListItem>,
    val automaticArchiving: Boolean
) : Parcelable