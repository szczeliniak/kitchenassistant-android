package pl.szczeliniak.kitchenassistant.android.network.responses.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Pagination(
    val pageNumber: Int,
    val limit: Int,
    val numberOfPages: Int
) : Parcelable