package pl.szczeliniak.kitchenassistant.android.network.requests

import java.time.LocalDate

data class UpdateShoppingListRequest(
    val name: String,
    val description: String?,
    val date: LocalDate?
)