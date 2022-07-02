package pl.szczeliniak.kitchenassistant.android.network.requests

import java.time.LocalDate

data class AddDayPlanRequest(
    val userId: Int,
    val date: LocalDate
)