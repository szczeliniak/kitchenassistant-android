package pl.szczeliniak.kitchenassistant.android.ui.utils

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

fun Toolbar.init(activity: AppCompatActivity, navigationIconId: Int, onNavigationClickListener: View.OnClickListener) {
    setNavigationIcon(navigationIconId)
    activity.setSupportActionBar(this)
    setNavigationOnClickListener { onNavigationClickListener.onClick(this) }
}