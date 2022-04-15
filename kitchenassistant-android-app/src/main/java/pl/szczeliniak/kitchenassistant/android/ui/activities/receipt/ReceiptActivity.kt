package pl.szczeliniak.kitchenassistant.android.ui.activities.receipt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityReceiptBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadReceiptEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Receipt
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.fragments.ReceiptActivityFragment
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.fragments.info.ReceiptInfoFragment
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.fragments.ingredients.ReceiptIngredientsFragment
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.fragments.steps.ReceiptStepsFragment
import pl.szczeliniak.kitchenassistant.android.ui.adapters.FragmentPagerAdapter
import pl.szczeliniak.kitchenassistant.android.ui.utils.ToolbarUtils.Companion.init
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class ReceiptActivity : AppCompatActivity() {

    companion object {
        private const val RECEIPT_ID_EXTRA = "RECEIPT_ID_EXTRA"

        fun start(context: Context, receiptId: Int) {
            val intent = Intent(context, ReceiptActivity::class.java)
            intent.putExtra(RECEIPT_ID_EXTRA, receiptId)
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var eventBus: EventBus

    @Inject
    lateinit var receiptActivityViewModelFactory: ReceiptActivityViewModel.Factory

    private lateinit var binding: ActivityReceiptBinding
    private val receiptLoadingStateHandler: LoadingStateHandler<Receipt> = prepareReceiptLoadingStateHandler()
    private val observers = mutableListOf<ReceiptActivityFragment>()

    private val viewModel: ReceiptActivityViewModel by viewModels() {
        ReceiptActivityViewModel.provideFactory(
            receiptActivityViewModelFactory,
            intent.getIntExtra(RECEIPT_ID_EXTRA, -1)
        )
    }

    var receipt: Receipt? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiptBinding.inflate(layoutInflater)
        initPager()
        setContentView(binding.root)

        viewModel.receipt.observe(this) { receiptLoadingStateHandler.handle(it) }
    }

    private fun prepareReceiptLoadingStateHandler(): LoadingStateHandler<Receipt> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<Receipt> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@ReceiptActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Receipt) {
                binding.toolbarLayout.toolbar.init(this@ReceiptActivity, data.name)
                receipt = data
                observers.forEach { it.onReceiptChanged() }
            }
        })
    }

    private fun initPager() {
        binding.viewPager.adapter = FragmentPagerAdapter(
            arrayOf(
                ReceiptInfoFragment.create(), ReceiptIngredientsFragment.create(), ReceiptStepsFragment.create()
            ), supportFragmentManager, lifecycle
        )

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val nameId = when (position) {
                0 -> {
                    R.string.title_fragment_receipt_info
                }
                1 -> {
                    R.string.title_fragment_receipt_ingredients
                }
                2 -> {
                    R.string.title_fragment_receipt_steps
                }
                else -> {
                    throw UnsupportedOperationException()
                }
            }
            tab.text = getString(nameId)
        }.attach()
    }

    fun addChangesObserver(fragment: ReceiptActivityFragment) {
        observers.add(fragment)
    }

    fun removeChangesObserver(fragment: ReceiptActivityFragment) {
        observers.remove(fragment)
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
    fun reloadReceiptEvent(event: ReloadReceiptEvent) {
        viewModel.reload()
    }

}