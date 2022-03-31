package pl.szczeliniak.kitchenassistant.android.network.responses

import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Receipt

data class ReceiptsResponse(
    val receipts: List<Receipt>
)