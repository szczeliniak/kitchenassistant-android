package pl.szczeliniak.kitchenassistant.android.network.responses

import pl.szczeliniak.kitchenassistant.android.network.responses.dto.DayPlan
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Pagination

data class DayPlansResponse(
    val dayPlans: List<DayPlan>,
    val pagination: Pagination
)