package pl.szczeliniak.kitchenassistant.android.ui.activities.addreceipt

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
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityAddReceiptBinding
import pl.szczeliniak.kitchenassistant.android.events.NewReceiptEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddReceiptRequest
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.ReceiptActivity
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.init
import pl.szczeliniak.kitchenassistant.android.ui.utils.showProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.toast
import javax.inject.Inject

@AndroidEntryPoint
class AddReceiptActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AddReceiptActivity::class.java)
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var localStorageService: LocalStorageService

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: AddReceiptActivityViewModel by viewModels()

    private lateinit var binding: ActivityAddReceiptBinding

    private val saveReceiptLoadingStateHandler = prepareSaveReceiptLoadingStateHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLayout()
    }

    private fun initLayout() {
        binding = ActivityAddReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.activityAddEditReceiptToolbar.toolbar.init(this, R.string.activity_new_receipt_title)
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

    private fun prepareSaveReceiptLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@AddReceiptActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner(this@AddReceiptActivity)
            }

            override fun onSuccess(data: Int) {
                eventBus.post(NewReceiptEvent())
                ReceiptActivity.start(this@AddReceiptActivity, data)
                finish()
            }
        })
    }

    private fun saveReceipt() {
        if (!validateData()) {
            return
        }
        addNewReceipt()
    }

    private fun validateData(): Boolean {
        if (name.isEmpty()) {
            toast(R.string.toast_receipt_name_is_empty)
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

}