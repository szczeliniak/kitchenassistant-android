package pl.szczeliniak.kitchenassistant.android.network.responses

import pl.szczeliniak.kitchenassistant.android.network.responses.dto.DayPlanReceipt

data class DayPlanReceiptsResponse(
    val receipts: List<DayPlanReceipt>
)