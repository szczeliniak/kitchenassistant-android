package pl.szczeliniak.kitchenassistant.android.ui.activities.addeditreceipt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.core.widget.doOnTextChanged
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityAddEditReceiptBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadReceiptsEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddReceiptRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateReceiptRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Category
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Receipt
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.ReceiptActivity
import pl.szczeliniak.kitchenassistant.android.ui.adapters.CategoryDropdownArrayAdapter
import pl.szczeliniak.kitchenassistant.android.ui.adapters.TagDropdownArrayAdapter
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatEditTextUtils.Companion.getTextOrNull
import pl.szczeliniak.kitchenassistant.android.ui.utils.ChipGroupUtils.Companion.add
import pl.szczeliniak.kitchenassistant.android.ui.utils.ChipGroupUtils.Companion.getTextInChips
import pl.szczeliniak.kitchenassistant.android.ui.utils.ToolbarUtils.Companion.init
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class AddEditReceiptActivity : AppCompatActivity() {

    companion object {
        private const val RECEIPT_EXTRA = "RECEIPT_EXTRA"

        fun start(context: Context, receipt: Receipt? = null) {
            val intent = Intent(context, AddEditReceiptActivity::class.java)
            receipt?.let { intent.putExtra(RECEIPT_EXTRA, it) }
            context.startActivity(intent)
        }
    }

    private val viewModel: AddEditReceiptActivityViewModel by viewModels()
    private val saveReceiptLoadingStateHandler = prepareSaveReceiptLoadingStateHandler()
    private val loadCategoriesLoadingStateHandler = prepareLoadCategoriesLoadingStateHandler()
    private val loadTagsLoadingStateHandler = prepareLoadTagsLoadingStateHandler()

    private val receipt: Receipt?
        get() {
            return intent.getParcelableExtra(RECEIPT_EXTRA)
        }

    @Inject
    lateinit var localStorageService: LocalStorageService

    @Inject
    lateinit var eventBus: EventBus
    private lateinit var binding: ActivityAddEditReceiptBinding
    private lateinit var categoriesDropdownAdapter: CategoryDropdownArrayAdapter
    private lateinit var tagsArrayAdapter: TagDropdownArrayAdapter

    private var selectedCategory: Category? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLayout()
    }

    private fun initLayout() {
        binding = ActivityAddEditReceiptBinding.inflate(layoutInflater)
        receipt?.let { r ->
            binding.toolbarLayout.toolbar.init(this@AddEditReceiptActivity, R.string.title_activity_edit_receipt)
            binding.receiptName.setText(r.name)
            binding.receiptDescription.setText(r.description)
            binding.receiptAuthor.setText(r.author)
            binding.receiptUrl.setText(r.source)
            r.tags.forEach { addTagChip(binding.tag, it) }
            viewModel.setCategory(r.category)
        } ?: kotlin.run {
            binding.toolbarLayout.toolbar.init(this@AddEditReceiptActivity, R.string.title_activity_new_receipt)
        }
        binding.receiptCategory.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.receiptCategory.text.toString().isEmpty() || selectedCategory == null) {
                    viewModel.setCategory(null)
                } else {
                    viewModel.setCategory(selectedCategory)
                }
            }
        }

        tagsArrayAdapter = TagDropdownArrayAdapter(this)
        binding.tag.setAdapter(tagsArrayAdapter)
        binding.tag.setOnKeyListener { _, keyCode, event -> onKeyInTagPressed(keyCode, event) }
        binding.tag.setOnItemClickListener { _, _, position, _ ->
            addTagChip(binding.tag, tagsArrayAdapter.getItem(position)!!)
        }
        viewModel.tags.observe(this) { loadTagsLoadingStateHandler.handle(it) }

        binding.receiptCategory.setOnItemClickListener { _, _, position, _ ->
            viewModel.setCategory(categoriesDropdownAdapter.getItem(position))
        }

        viewModel.categories.observe(this) { loadCategoriesLoadingStateHandler.handle(it) }
        viewModel.selectedCategory.observe(this) {
            selectedCategory = it
            binding.receiptCategory.setText(it?.name ?: "")
        }

        categoriesDropdownAdapter = CategoryDropdownArrayAdapter(this)
        binding.receiptCategory.setAdapter(categoriesDropdownAdapter)

        binding.receiptName.doOnTextChanged { _, _, _, _ ->
            if (!isNameValid()) {
                binding.receiptNameLayout.error = getString(R.string.message_receipt_name_is_empty)
            } else {
                binding.receiptNameLayout.error = null
            }
        }

        setContentView(binding.root)
    }

    private fun onKeyInTagPressed(keyCode: Int, event: KeyEvent): Boolean {
        if (isChipToBeAdded(binding.tag, keyCode, event)) {
            addTagChip(binding.tag, binding.tag.text.toString())
            return true
        }
        return false
    }

    private fun isChipToBeAdded(textView: AppCompatAutoCompleteTextView, keyCode: Int, event: KeyEvent): Boolean {
        return event.action == KeyEvent.ACTION_DOWN
                && keyCode == KeyEvent.KEYCODE_ENTER
                && textView.text.toString().isNotBlank()
    }

    private fun addTagChip(textView: AppCompatAutoCompleteTextView, item: String) {
        binding.tagChips.add(layoutInflater, item, true)
        textView.setText("")
    }

    private fun isNameValid(): Boolean {
        return !name.isNullOrEmpty()
    }

    private fun prepareSaveReceiptLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@AddEditReceiptActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
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

    private fun prepareLoadCategoriesLoadingStateHandler(): LoadingStateHandler<List<Category>> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<List<Category>> {
            override fun onSuccess(data: List<Category>) {
                categoriesDropdownAdapter.refresh(data)
            }
        })
    }

    private fun prepareLoadTagsLoadingStateHandler(): LoadingStateHandler<List<String>> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<List<String>> {
            override fun onSuccess(data: List<String>) {
                tagsArrayAdapter.refresh(data)
            }
        })
    }

    private fun saveReceipt() {
        if (!isNameValid()) {
            return
        }
        receipt?.let { r ->
            viewModel.updateReceipt(
                r.id,
                UpdateReceiptRequest(name!!, author, url, description, selectedCategory?.id, tags)
            ).observe(this) { saveReceiptLoadingStateHandler.handle(it) }
        } ?: kotlin.run {
            viewModel.addReceipt(
                AddReceiptRequest(
                    name!!,
                    author,
                    url,
                    description,
                    localStorageService.getId(),
                    selectedCategory?.id,
                    tags
                )
            ).observe(this) { saveReceiptLoadingStateHandler.handle(it) }
        }
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

    private val tags: List<String>
        get() {
            return binding.tagChips.getTextInChips()
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