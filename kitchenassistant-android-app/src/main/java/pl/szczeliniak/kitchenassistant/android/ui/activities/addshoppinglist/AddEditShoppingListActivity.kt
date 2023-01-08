package pl.szczeliniak.kitchenassistant.android.ui.activities.addshoppinglist

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityAddEditShoppingListBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadShoppingListsEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddShoppingListRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateShoppingListRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ShoppingListDetails
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.ui.activities.shoppinglist.ShoppingListActivity
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatEditTextUtils.Companion.getTextOrNull
import pl.szczeliniak.kitchenassistant.android.ui.utils.ContextUtils.Companion.toast
import pl.szczeliniak.kitchenassistant.android.ui.utils.ToolbarUtils.Companion.init
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import pl.szczeliniak.kitchenassistant.android.utils.LocalDateUtils
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class AddEditShoppingListActivity : AppCompatActivity() {

    companion object {
        private const val SHOPPING_LIST_ID_EXTRA = "SHOPPING_LIST_ID_EXTRA"

        fun start(context: Context, shoppingListId: Int? = null) {
            val intent = Intent(context, AddEditShoppingListActivity::class.java)
            shoppingListId?.let { intent.putExtra(SHOPPING_LIST_ID_EXTRA, it) }
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var localStorageService: LocalStorageService

    @Inject
    lateinit var eventBus: EventBus

    @Inject
    lateinit var factory: AddEditShoppingListActivityViewModel.Factory

    private val viewModel: AddEditShoppingListActivityViewModel by viewModels {
        AddEditShoppingListActivityViewModel.provideFactory(factory, shoppingListId)
    }

    private val saveShoppingListLoadingStateHandler = prepareSaveShoppingListLoadingStateHandler()
    private val loadShoppingListLoadingStateHandler = prepareLoadShoppingListLoadingStateHandler()

    private lateinit var binding: ActivityAddEditShoppingListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLayout()

        viewModel.shoppingList.observe(this) { loadShoppingListLoadingStateHandler.handle(it) }
    }

    private fun initLayout() {
        binding = ActivityAddEditShoppingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.shoppingListDate.setOnClickListener {
            val date = this.date ?: LocalDate.now()
            val dialog = DatePickerDialog(this@AddEditShoppingListActivity, { _, year, month, dayOfMonth ->
                binding.shoppingListDate.text = LocalDateUtils.stringify(LocalDate.of(year, month + 1, dayOfMonth))
            }, date.year, date.monthValue - 1, date.dayOfMonth)
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.label_button_cancel)) { _, _ ->
                binding.shoppingListDate.text = getString(R.string.label_button_select_date)
                binding.shoppingListAutomaticArchiving.isChecked = false
            }
            dialog.show()
        }

        binding.shoppingListAutomaticArchiving.setOnCheckedChangeListener { _, isChecked ->
            if(date == null && isChecked) {
                toast(R.string.message_cannot_auto_archive_shopping_list_without_date)
                binding.shoppingListAutomaticArchiving.isChecked = false
            }
        }

        binding.toolbarLayout.toolbar.init(this, R.string.title_activity_new_shopping_list)
        binding.shoppingListName.doOnTextChanged { _, _, _, _ ->
            checkName()
        }
    }

    private fun checkName() {
        if (name.isNullOrEmpty()) {
            binding.shoppingListNameLayout.error = getString(R.string.message_shopping_list_name_is_empty)
        } else {
            binding.shoppingListNameLayout.error = null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_add_shopping_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save) {
            saveShoppingList()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun prepareSaveShoppingListLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@AddEditShoppingListActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                eventBus.post(ReloadShoppingListsEvent())
                if (shoppingListId == null) {
                    ShoppingListActivity.start(this@AddEditShoppingListActivity, data)
                }
                finish()
            }
        })
    }

    private fun prepareLoadShoppingListLoadingStateHandler(): LoadingStateHandler<ShoppingListDetails> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<ShoppingListDetails> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@AddEditShoppingListActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: ShoppingListDetails) {
                binding.shoppingListName.setText(data.name)
                binding.shoppingListDescription.setText(data.description)
                data.date?.let { date ->
                    binding.shoppingListDate.text = LocalDateUtils.stringify(
                        LocalDate.of(
                            date.year,
                            date.monthValue,
                            date.dayOfMonth
                        )
                    )
                }
                binding.toolbarLayout.toolbar.init(
                    this@AddEditShoppingListActivity,
                    R.string.title_activity_edit_shopping_list
                )
                binding.shoppingListAutomaticArchiving.isChecked = data.automaticArchiving
            }
        })
    }

    private fun saveShoppingList() {
        checkName()
        if (binding.shoppingListNameLayout.error != null) {
            return
        }
        shoppingListId?.let {
            viewModel.updateShoppingList(it, UpdateShoppingListRequest(name!!, description, date, automaticArchiving))
                .observe(this) { state -> saveShoppingListLoadingStateHandler.handle(state) }
        } ?: kotlin.run {
            viewModel.addShoppingList(
                AddShoppingListRequest(
                    name!!,
                    description,
                    localStorageService.getId(),
                    date,
                    automaticArchiving
                )
            )
                .observe(this) { saveShoppingListLoadingStateHandler.handle(it) }
        }
    }

    private val name: String?
        get() {
            return binding.shoppingListName.getTextOrNull()
        }

    private val description: String?
        get() {
            return binding.shoppingListDescription.getTextOrNull()
        }

    private val date: LocalDate?
        get() {
            val asString = binding.shoppingListDate.text.toString()
            return if (LocalDateUtils.parsable(asString)) LocalDateUtils.parse(asString) else null
        }

    private val shoppingListId: Int?
        get() {
            val id = intent.getIntExtra(SHOPPING_LIST_ID_EXTRA, -1)
            return if (id <= 0) null else id
        }

    private val automaticArchiving: Boolean
        get() {
            return binding.shoppingListAutomaticArchiving.isChecked
        }

}