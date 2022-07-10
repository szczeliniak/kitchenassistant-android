package pl.szczeliniak.kitchenassistant.android.network.requests

import java.time.LocalDate

data class UpdateDayPlanRequest(
    val name: String,
    val description: String?,
    val date: LocalDate?,
    val automaticArchiving: Boolean
)