package pl.szczeliniak.kitchenassistant.android.ui.utils

class PaginationHandler(
    var onLoad: OnLoad
) {

    companion object {
        private const val DEFAULT_PAGE = 1
    }

    private var page: Int = DEFAULT_PAGE
    var maxPage: Int = DEFAULT_PAGE

    fun load() {
        if (page >= maxPage) {
            return
        }
        page += 1
        onLoad.onLoad(page)
    }

    fun reset() {
        page = 0
        load()
    }

    fun interface OnLoad {
        fun onLoad(page: Int)
    }
}