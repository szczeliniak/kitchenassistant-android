package pl.szczeliniak.kitchenassistant.android.ui.activities.dayplan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityDayPlanBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadDayPlansEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.DayPlanDetails
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.ReceiptActivity
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.choosereceipttodayplan.ChooseReceiptToDayPlanDialog
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.confirmation.ConfirmationDialog
import pl.szczeliniak.kitchenassistant.android.ui.listitems.DayPlanReceiptItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatTextViewUtils.Companion.fillOrHide
import pl.szczeliniak.kitchenassistant.android.ui.utils.ToolbarUtils.Companion.init
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import pl.szczeliniak.kitchenassistant.android.utils.LocalDateUtils
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

    private val receiptsAdapter = GroupAdapter<GroupieViewHolder>()

    @Inject
    lateinit var eventBus: EventBus

    @Inject
    lateinit var dayPlanActivityViewModelFactory: DayPlanActivityViewModel.Factory

    private lateinit var binding: ActivityDayPlanBinding
    private val dayPlanLoadingStateHandler: LoadingStateHandler<DayPlanDetails> = prepareDayPlanLoadingStateHandler()
    private val archiveDayPlanStateHandler: LoadingStateHandler<Int> = prepareArchiveDayPlanStateHandler()
    private val assignDeassignReceiptFromDayPlanStateHandler: LoadingStateHandler<Int> =
        assignDeassignReceiptFromDayPlanStateHandler()

    private val viewModel: DayPlanActivityViewModel by viewModels {
        DayPlanActivityViewModel.provideFactory(dayPlanActivityViewModelFactory, dayPlanId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLayout()
        viewModel.dayPlan.observe(this) { dayPlanLoadingStateHandler.handle(it) }
    }

    private fun initLayout() {
        binding = ActivityDayPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.adapter = receiptsAdapter

        binding.buttonAddReceiptToDayPlan.setOnClickListener {
            ChooseReceiptToDayPlanDialog.show(
                supportFragmentManager,
                ChooseReceiptToDayPlanDialog.OnReceiptChosen { id ->
                    viewModel.assignReceipt(id).observe(this@DayPlanActivity) {
                        assignDeassignReceiptFromDayPlanStateHandler.handle(it)
                    }
                })
        }
    }

    private fun prepareDayPlanLoadingStateHandler(): LoadingStateHandler<DayPlanDetails> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<DayPlanDetails> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@DayPlanActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: DayPlanDetails) {
                binding.toolbarLayout.toolbar.init(this@DayPlanActivity, data.name)
                binding.dayPlanDate.fillOrHide(LocalDateUtils.stringify(data.date), binding.dayPlanDate)
                binding.dayPlanName.text = data.name
                binding.dayPlanDescription.fillOrHide(data.description, binding.dayPlanDescriptionLayout)

                receiptsAdapter.clear()
                if (data.receipts.isEmpty()) {
                    binding.dayPlanReceiptsLayout.showEmptyIcon(this@DayPlanActivity)
                } else {
                    binding.dayPlanReceiptsLayout.hideEmptyIcon()
                    data.receipts.forEach { item ->
                        receiptsAdapter.add(
                            DayPlanReceiptItem(
                                this@DayPlanActivity, item, { receipt ->
                                    ReceiptActivity.start(this@DayPlanActivity, receipt.id)
                                }, { receipt ->
                                    ConfirmationDialog.show(supportFragmentManager) {
                                        viewModel.deassignReceipt(dayPlanId, receipt.id)
                                            .observe(this@DayPlanActivity) {
                                                assignDeassignReceiptFromDayPlanStateHandler.handle(it)
                                            }
                                    }
                                }
                            ))
                    }
                }
            }
        })
    }

    private fun assignDeassignReceiptFromDayPlanStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@DayPlanActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                viewModel.reload()
            }
        })
    }

    private fun prepareArchiveDayPlanStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@DayPlanActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                eventBus.post(ReloadDayPlansEvent())
                finish()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_day_plan, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.archive) {
            ConfirmationDialog.show(supportFragmentManager) {
                viewModel.archive(dayPlanId).observe(this) { archiveDayPlanStateHandler.handle(it) }
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private val dayPlanId: Int
        get() {
            return intent.getIntExtra(DAY_PLAN_ID_EXTRA, -1)
        }

}