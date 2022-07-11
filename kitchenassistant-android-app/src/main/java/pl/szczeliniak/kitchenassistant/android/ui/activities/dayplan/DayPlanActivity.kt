package pl.szczeliniak.kitchenassistant.android.ui.activities.dayplan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityDayPlanBinding
import pl.szczeliniak.kitchenassistant.android.events.DayPlanReloadedEvent
import pl.szczeliniak.kitchenassistant.android.ui.adapters.FragmentPagerAdapter
import pl.szczeliniak.kitchenassistant.android.ui.fragments.dayplaninfo.DayPlanInfoFragment
import pl.szczeliniak.kitchenassistant.android.ui.fragments.dayplaningredients.DayPlanIngredientsFragment
import pl.szczeliniak.kitchenassistant.android.ui.utils.ToolbarUtils.Companion.init
import javax.inject.Inject

@AndroidEntryPoint
class DayPlanActivity : AppCompatActivity() {

    companion object {
        private const val DAY_PLAN_ID_EXTRA = "DAY_PLAN_ID_EXTRA"

        fun start(context: Context, dayPlanId: Int) {
            val intent = Intent(context, DayPlanActivity::class.java)
            intent.putExtra(DAY_PLAN_ID_EXTRA, dayPlanId)
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var eventBus: EventBus

    private lateinit var binding: ActivityDayPlanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDayPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initPager()
    }

    private fun initPager() {
        val dayPlanId = intent.getIntExtra(DAY_PLAN_ID_EXTRA, -1)
        binding.viewPager.adapter = FragmentPagerAdapter(
            arrayOf(
                DayPlanInfoFragment.create(dayPlanId), DayPlanIngredientsFragment.create(dayPlanId)
            ), supportFragmentManager, lifecycle
        )

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val nameId = when (position) {
                0 -> {
                    R.string.title_fragment_day_plan_info
                }
                1 -> {
                    R.string.title_fragment_day_plan_ingredients
                }
                else -> {
                    throw UnsupportedOperationException()
                }
            }
            tab.text = getString(nameId)
        }.attach()
    }

    override fun onStart() {
        eventBus.register(this)
        super.onStart()
    }

    override fun onStop() {
        eventBus.unregister(this)
        super.onStop()
    }

    @Subscribe
    fun dayPlanReloaded(event: DayPlanReloadedEvent) {
        binding.toolbarLayout.toolbar.init(this@DayPlanActivity, event.data.name)
    }

}