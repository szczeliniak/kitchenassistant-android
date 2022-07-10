package pl.szczeliniak.kitchenassistant.android.network.requests

import java.time.LocalDate

data class AddShoppingListRequest(
    val name: String,
    val description: String?,
    val userId: Int,
    val date: LocalDate?,
    val automaticArchiving: Boolean
)