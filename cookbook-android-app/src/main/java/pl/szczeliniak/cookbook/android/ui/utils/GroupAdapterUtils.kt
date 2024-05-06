package pl.szczeliniak.cookbook.android.ui.utils

import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.viewbinding.BindableItem

class GroupAdapterUtils {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T : BindableItem<*>> GroupAdapter<GroupieViewHolder>.getItems(): List<T> {
            val items = ArrayList<T>()
            for (i in 0 until itemCount) {
                items.add((getItem(i) as T))
            }
            return items
        }
    }

}