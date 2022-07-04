package pl.szczeliniak.kitchenassistant.android.network.responses.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class DayPlanDetails(
    val id: Int,
    val date: LocalDate?,
    val receipts: List<DayPlanReceipt>
) : Parcelable