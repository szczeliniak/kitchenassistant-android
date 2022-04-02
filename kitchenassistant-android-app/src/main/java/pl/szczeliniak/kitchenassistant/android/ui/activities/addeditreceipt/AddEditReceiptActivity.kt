package pl.szczeliniak.kitchenassistant.android.ui.activities.addeditreceipt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.greenrobot.eventbus.EventBus
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityAddEditReceiptBinding
import pl.szczeliniak.kitchenassistant.android.events.NewReceiptEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddReceiptRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateReceiptRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Receipt
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.ReceiptActivity
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.init
import pl.szczeliniak.kitchenassistant.android.ui.utils.showProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.toast
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddEditReceiptActivity : AppCompatActivity() {

    companion object {
        private const val RECEIPT_ID_EXTRA = "RECEIPT_ID_EXTRA"

        fun start(context: Context) {
            val intent = Intent(context, AddEditReceiptActivity::class.java)
            context.startActivity(intent)
        }

        fun start(context: Context, receiptId: Int) {
            val intent = Intent(context, AddEditReceiptActivity::class.java)
            intent.putExtra(RECEIPT_ID_EXTRA, receiptId)
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var localStorageService: LocalStorageService

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: AddEditReceiptActivityViewModel by viewModels()

    private lateinit var binding: ActivityAddEditReceiptBinding

    private val saveReceiptLoadingStateHandler = prepareSaveReceiptLoadingStateHandler()
    private val loadReceiptLoadingStateHandler = prepareLoadReceiptLoadingStateHandler()

    private val receiptId: Int
        get() {
            return intent.getIntExtra(RECEIPT_ID_EXTRA, -1)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLayout()

        if (receiptId > 0) {
            loadReceipt(receiptId)
        }
    }

    private fun initLayout() {
        binding = ActivityAddEditReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val titleId: Int = if (receiptId > 0) {
            R.string.activity_edit_receipt_title
        } else {
            R.string.activity_new_receipt_title
        }
        binding.activityAddEditReceiptToolbar.toolbar.init(this, titleId)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_add_edit_receipt, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.activity_add_edit_receipt_menu_item_save) {
            saveReceipt()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadReceipt(receiptId: Int) {
        viewModel.receipt.observe(this) { loadReceiptLoadingStateHandler.handle(it) }
        viewModel.reloadReceipt(receiptId)
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
                eventBus.post(NewReceiptEvent())
                ReceiptActivity.start(this@AddEditReceiptActivity, data)
                finish()
            }
        })
    }

    private fun prepareLoadReceiptLoadingStateHandler(): LoadingStateHandler<Receipt> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<Receipt> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@AddEditReceiptActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner(this@AddEditReceiptActivity)
            }

            override fun onSuccess(data: Receipt) {
                binding.activityAddEditReceiptEdittextReceiptName.setText(data.name)
                binding.activityAddEditReceiptEdittextAuthor.setText(data.author)
                binding.activityAddEditReceiptEdittextUrl.setText(data.source)
                binding.activityAddEditReceiptEdittextDescription.setText(data.description)
            }
        })
    }

    private fun saveReceipt() {
        if (!validateData()) {
            return
        }
        if (receiptId > 0) {
            updateReceipt()
        } else {
            addNewReceipt()
        }
    }

    private fun validateData(): Boolean {
        if (name.isEmpty()) {
            toast(R.string.activity_add_edit_receipt_error_name_is_empty)
            return false
        }
        return true
    }

    private fun addNewReceipt() {
        viewModel.addReceipt(AddReceiptRequest(name, author, url, description, localStorageService.getId()))
            .observe(this) { saveReceiptLoadingStateHandler.handle(it) }
    }

    private val name: String
        get() {
            return binding.activityAddEditReceiptEdittextReceiptName.text.toString()
        }

    private val author: String
        get() {
            return binding.activityAddEditReceiptEdittextAuthor.text.toString()
        }

    private val url: String
        get() {
            return binding.activityAddEditReceiptEdittextUrl.text.toString()
        }

    private val description: String
        get() {
            return binding.activityAddEditReceiptEdittextDescription.text.toString()
        }

    private fun updateReceipt() {
        viewModel.updateReceipt(
            receiptId,
            UpdateReceiptRequest(name, author, url, description, localStorageService.getId())
        )
            .observe(this) { saveReceiptLoadingStateHandler.handle(it) }
    }

}