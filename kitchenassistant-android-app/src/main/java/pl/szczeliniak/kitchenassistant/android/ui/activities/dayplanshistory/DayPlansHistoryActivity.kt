package pl.szczeliniak.kitchenassistant.android.ui.activities.dayplanshistory

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityDayPlansHistoryBinding
import pl.szczeliniak.kitchenassistant.android.events.DayPlanEditedEvent
import pl.szczeliniak.kitchenassistant.android.listeners.EndlessScrollRecyclerViewListener
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.DayPlansResponse
import pl.szczeliniak.kitchenassistant.android.ui.activities.dayplan.DayPlanActivity
import pl.szczeliniak.kitchenassistant.android.ui.listitems.DayPlanItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.ToolbarUtils.Companion.init
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class
DayPlansHistoryActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, DayPlansHistoryActivity::class.java)
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: DayPlansHistoryActivityViewModel by viewModels()
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val loadDayPlansLoadingStateHandler = prepareLoadDayPlansStateHandler()
    private val deleteDayPlanLoadingStateHandler = prepareDeleteDayPlanLoadingStateHandler()

    private lateinit var endlessScrollRecyclerViewListener: EndlessScrollRecyclerViewListener
    private lateinit var binding: ActivityDayPlansHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLayout()
        viewModel.dayPlans.observe(this) { loadDayPlansLoadingStateHandler.handle(it) }
    }

    private fun initLayout() {
        binding = ActivityDayPlansHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbarLayout.toolbar.init(this, R.string.title_activity_dayplans_history)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(binding.recyclerView.context, DividerItemDecoration.VERTICAL)
        )

        endlessScrollRecyclerViewListener = EndlessScrollRecyclerViewListener(
            binding.recyclerView.layoutManager as LinearLayoutManager,
            { viewModel.reloadDayPlans(it) },
            { adapter.clear() }
        )

        binding.recyclerView.addOnScrollListener(endlessScrollRecyclerViewListener)

        binding.refreshLayout.setOnRefreshListener { endlessScrollRecyclerViewListener.reset() }
    }

    private fun prepareLoadDayPlansStateHandler(): LoadingStateHandler<DayPlansResponse> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<DayPlansResponse> {
            override fun onInProgress() {
                binding.refreshLayout.isRefreshing = true
            }

            override fun onFinish() {
                binding.refreshLayout.isRefreshing = false
            }

            override fun onSuccess(data: DayPlansResponse) {
                endlessScrollRecyclerViewListener.maxPage = data.dayPlans.totalNumberOfPages
                if (data.dayPlans.items.isEmpty()) {
                    binding.layout.showEmptyIcon(this@DayPlansHistoryActivity)
                } else {
                    binding.layout.hideEmptyIcon()
                    data.dayPlans.items.forEach { dayPlan ->
                        adapter.add(DayPlanItem(this@DayPlansHistoryActivity, dayPlan, {
                            DayPlanActivity.start(this@DayPlansHistoryActivity, dayPlan.id, false)
                        }))
                    }
                }
            }
        })
    }

    private fun prepareDeleteDayPlanLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(this@DayPlansHistoryActivity, object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.layout.showProgressSpinner(this@DayPlansHistoryActivity)
            }

            override fun onFinish() {
                binding.layout.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                endlessScrollRecyclerViewListener.reset()
            }
        })
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
    fun onDayPlanEdited(event: DayPlanEditedEvent) {
        endlessScrollRecyclerViewListener.reset()
    }

}