package pl.szczeliniak.kitchenassistant.android.ui.utils

import android.widget.ArrayAdapter

class ArrayAdapterUtils {

    companion object {
        fun <T> ArrayAdapter<T>.getItems(): List<T> {
            val list = ArrayList<T>()
            for (i in 0 until count) {
                getItem(i)?.let { list.add(it as T) }
            }
            return list
        }
    }

}

