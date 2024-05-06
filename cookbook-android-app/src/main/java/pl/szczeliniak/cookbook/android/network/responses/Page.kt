package pl.szczeliniak.cookbook.android.network.responses

data class Page<T>(
    val pageNumber: Long,
    val pageSize: Int,
    val totalNumberOfPages: Long,
    val items: Collection<T>
)