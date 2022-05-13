package pl.szczeliniak.kitchenassistant.android.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ComponentToolbarBinding

class ToolbarComponent(context: Context, attributeSet: AttributeSet?) : FrameLayout(context, attributeSet) {

    val binding: ComponentToolbarBinding

    init {
        binding = ComponentToolbarBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun init(
        activity: AppCompatActivity,
        navigationIconId: Int,
        onNavigationClickListener: OnClickListener
    ) {
        binding.toolbar.setNavigationIcon(navigationIconId)
        activity.setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { onNavigationClickListener.onClick(this) }
    }

    fun init(activity: AppCompatActivity, resId: Int) {
        binding.toolbar.title = activity.getString(resId)
        init(activity, R.drawable.icon_arrow_back) {
            activity.onBackPressed()
        }
    }

    fun init(activity: AppCompatActivity, name: String) {
        binding.toolbar.title = name
        init(activity, R.drawable.icon_arrow_back) {
            activity.onBackPressed()
        }
    }

}