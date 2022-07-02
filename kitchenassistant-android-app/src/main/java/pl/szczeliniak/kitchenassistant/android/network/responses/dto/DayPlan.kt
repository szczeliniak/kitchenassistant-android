package pl.szczeliniak.kitchenassistant.android.network.responses.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class DayPlan(
    val id: Int,
    val date: LocalDate
) : Parcelable