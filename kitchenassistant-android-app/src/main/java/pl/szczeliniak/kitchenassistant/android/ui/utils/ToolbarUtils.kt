package pl.szczeliniak.kitchenassistant.android.ui.utils

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import pl.szczeliniak.kitchenassistant.android.R

fun Toolbar.init(activity: AppCompatActivity, navigationIconId: Int, onNavigationClickListener: View.OnClickListener) {
    setNavigationIcon(navigationIconId)
    activity.setSupportActionBar(this)
    setNavigationOnClickListener { onNavigationClickListener.onClick(this) }
}

fun Toolbar.init(activity: AppCompatActivity, resId: Int) {
    this.title = activity.getString(resId)
    init(activity, R.drawable.icon_arrow_back) {
        activity.onBackPressed()
    }
}

fun Toolbar.init(activity: AppCompatActivity, name: String) {
    this.title = name
    init(activity, R.drawable.icon_arrow_back) {
        activity.onBackPressed()
    }
}