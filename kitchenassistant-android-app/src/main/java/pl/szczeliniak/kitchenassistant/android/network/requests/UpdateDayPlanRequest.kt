package pl.szczeliniak.kitchenassistant.android.network.requests

import java.time.LocalDate

data class UpdateDayPlanRequest(
    val date: LocalDate
)