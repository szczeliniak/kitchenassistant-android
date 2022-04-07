package pl.szczeliniak.kitchenassistant.android.ui.activities.addeditreceipt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityAddEditReceiptBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadReceiptsEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddReceiptRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateReceiptRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Receipt
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.ReceiptActivity
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatEditTextUtils.Companion.getTextOrNull
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.init
import pl.szczeliniak.kitchenassistant.android.ui.utils.showProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.toast
import javax.inject.Inject

@AndroidEntryPoint
class AddEditReceiptActivity : AppCompatActivity() {

    companion object {
        private const val RECEIPT_EXTRA = "RECEIPT_EXTRA"

        fun start(context: Context) {
            val intent = Intent(context, AddEditReceiptActivity::class.java)
            context.startActivity(intent)
        }

        fun start(context: Context, receipt: Receipt) {
            val intent = Intent(context, AddEditReceiptActivity::class.java)
            intent.putExtra(RECEIPT_EXTRA, receipt)
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var localStorageService: LocalStorageService

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: AddEditReceiptActivityViewModel by viewModels()
    private val saveReceiptLoadingStateHandler = prepareSaveReceiptLoadingStateHandler()

    private lateinit var binding: ActivityAddEditReceiptBinding

    private val receipt: Receipt?
        get() {
            return intent.getParcelableExtra(RECEIPT_EXTRA)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLayout()
    }

    private fun initLayout() {
        binding = ActivityAddEditReceiptBinding.inflate(layoutInflater)
        receipt?.let { id ->
            binding.toolbarLayout.toolbar.init(this@AddEditReceiptActivity, R.string.title_activity_edit_receipt)
            binding.receiptName.setText(id.name)
            binding.receiptDescription.setText(id.description)
            binding.receiptAuthor.setText(id.author)
            binding.receiptUrl.setText(id.source)
        } ?: kotlin.run {
            binding.toolbarLayout.toolbar.init(this@AddEditReceiptActivity, R.string.title_activity_new_receipt)
        }
        setContentView(binding.root)
    }

    private fun prepareSaveReceiptLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@AddEditReceiptActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner(this@AddEditReceiptActivity)
            }

            override fun onSuccess(data: Int) {
                eventBus.post(ReloadReceiptsEvent())
                if (receipt == null) {
                    ReceiptActivity.start(this@AddEditReceiptActivity, data)
                }
                finish()
            }
        })
    }

    private fun saveReceipt() {
        if (!validateData()) {
            return
        }

        receipt?.let { r ->
            viewModel.updateReceipt(r.id, UpdateReceiptRequest(name!!, author, url, description))
                .observe(this) { saveReceiptLoadingStateHandler.handle(it) }
        } ?: kotlin.run {
            viewModel.addReceipt(AddReceiptRequest(name!!, author, url, description, localStorageService.getId()))
                .observe(this) { saveReceiptLoadingStateHandler.handle(it) }
        }
    }

    private fun validateData(): Boolean {
        if (name.isNullOrEmpty()) {
            toast(R.string.message_receipt_name_is_empty)
            return false
        }
        return true
    }

    private val name: String?
        get() {
            return binding.receiptName.getTextOrNull()
        }

    private val author: String?
        get() {
            return binding.receiptAuthor.getTextOrNull()
        }

    private val url: String?
        get() {
            return binding.receiptUrl.getTextOrNull()
        }

    private val description: String?
        get() {
            return binding.receiptDescription.getTextOrNull()
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_add_receipt, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.activity_add_receipt_menu_item_save) {
            saveReceipt()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}