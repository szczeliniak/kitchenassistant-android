package pl.szczeliniak.kitchenassistant.android.listeners

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EndlessScrollRecyclerViewListener(
    private val linearLayoutManager: LinearLayoutManager, var onLoad: (page: Int) -> Unit, var onReset: () -> Unit
) : RecyclerView.OnScrollListener() {

    companion object {
        private const val DEFAULT_PAGE = 1
    }

    private var page: Int = DEFAULT_PAGE
    var maxPage: Int = DEFAULT_PAGE

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dy < 1) {
            return
        }
        if ((linearLayoutManager.childCount + linearLayoutManager.findFirstVisibleItemPosition()) >= linearLayoutManager.itemCount) {
            load()
        }
    }

    fun reset() {
        linearLayoutManager.scrollToPosition(0)
        onReset()
        page = 0
        maxPage = DEFAULT_PAGE
        load()
    }

    private fun load() {
        if (page >= maxPage) {
            return
        }
        page += 1
        onLoad(page)
    }

}