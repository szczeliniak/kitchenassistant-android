package pl.szczeliniak.kitchenassistant.android.ui.activities.receipt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityReceiptBinding
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Receipt
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.fragments.ReceiptActivityFragment
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.fragments.ReceiptInfoFragment
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.fragments.ReceiptIngredientsFragment
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.fragments.ReceiptStepsFragment
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.init
import pl.szczeliniak.kitchenassistant.android.ui.utils.showProgressSpinner

@ExperimentalCoroutinesApi
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

    private lateinit var binding: ActivityReceiptBinding
    private lateinit var receiptLoadingStateHandler: LoadingStateHandler<Receipt>
    private val observers = mutableListOf<ReceiptActivityFragment>()

    private val viewModel: ReceiptActivityViewModel by viewModels()

    private val receiptId: Int
        get() {
            return intent.getIntExtra(RECEIPT_ID_EXTRA, -1)
        }

    var receipt: Receipt? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiptBinding.inflate(layoutInflater)
        binding.activityReceiptToolbarLayout.toolbar.init(this, "qwerty")
        initPager()
        setContentView(binding.root)

        receiptLoadingStateHandler = prepareReceiptLoadingStateHandler()
        viewModel.receipt.observe(this) { receiptLoadingStateHandler.handle(it) }
        reload()
    }

    private fun prepareReceiptLoadingStateHandler(): LoadingStateHandler<Receipt> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<Receipt> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@ReceiptActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner(this@ReceiptActivity)
            }

            override fun onSuccess(data: Receipt) {
                binding.activityReceiptToolbarLayout.toolbar.init(this@ReceiptActivity, data.name)
                receipt = data
                observers.forEach { it.onReceiptChanged() }
            }
        })
    }

    private fun reload() {
        viewModel.load(receiptId)
    }

    private fun initPager() {
        binding.activityReceiptViewPager.adapter = MainActivityPagerAdapter(
            arrayOf(
                ReceiptInfoFragment(), ReceiptIngredientsFragment(), ReceiptStepsFragment()
            ), supportFragmentManager, lifecycle
        )

        TabLayoutMediator(binding.activityReceiptTabLayout, binding.activityReceiptViewPager) { tab, position ->
            val titleId = when (position) {
                0 -> {
                    R.string.fragment_receipt_info_title
                }
                1 -> {
                    R.string.fragment_receipt_ingredients_title
                }
                2 -> {
                    R.string.fragment_receipt_steps_title
                }
                else -> {
                    throw UnsupportedOperationException()
                }
            }
            tab.text = getString(titleId)
        }.attach()
    }

    fun addChangesObserver(fragment: ReceiptActivityFragment) {
        observers.add(fragment)
    }

    fun removeChangesObserver(fragment: ReceiptActivityFragment) {
        observers.remove(fragment)
    }

}