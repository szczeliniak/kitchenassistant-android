package pl.szczeliniak.kitchenassistant.android.listeners

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EndlessScrollRecyclerViewListener(
    private val linearLayoutManager: LinearLayoutManager,
    private val onLoadMore: OnLoadMore
) : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dy < 1) {
            return
        }
        if ((linearLayoutManager.childCount + linearLayoutManager.findFirstVisibleItemPosition()) >= linearLayoutManager.itemCount
        ) {
            onLoadMore.onLoadMore()
        }
    }

    fun interface OnLoadMore {
        fun onLoadMore()
    }

}