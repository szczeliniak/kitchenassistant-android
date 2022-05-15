package pl.szczeliniak.kitchenassistant.android.ui.components.layouts

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class KaRecyclerViewLayout(context: Context, attributeSet: AttributeSet) :
    RecyclerView(context, attributeSet) {

    init {
        layoutManager = LinearLayoutManager(context)
    }

}