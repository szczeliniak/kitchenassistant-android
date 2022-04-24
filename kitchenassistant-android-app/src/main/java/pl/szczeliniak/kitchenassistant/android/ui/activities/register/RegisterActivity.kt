package pl.szczeliniak.kitchenassistant.android.ui.activities.register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import dagger.hilt.android.AndroidEntryPoint
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityRegisterBinding
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.RegisterRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.LoginResponse
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.ui.activities.main.MainActivity
import pl.szczeliniak.kitchenassistant.android.ui.utils.ButtonUtils.Companion.enable
import pl.szczeliniak.kitchenassistant.android.ui.utils.ContextUtils.Companion.toast
import pl.szczeliniak.kitchenassistant.android.ui.utils.ToolbarUtils.Companion.init
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import pl.szczeliniak.kitchenassistant.android.utils.ValidationUtils
import retrofit2.HttpException
import javax.inject.Inject

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, RegisterActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityRegisterBinding
    private val registerStateHandler: LoadingStateHandler<LoginResponse> = prepareRegisterStateHandler()

    private val viewModel: RegisterActivityViewModel by viewModels()

    @Inject
    lateinit var localStorageService: LocalStorageService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initLayout()

        checkButtonState()
    }

    private fun initLayout() {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarLayout.toolbar.init(this, R.string.title_activity_register)
        binding.buttonRegister.setOnClickListener { handleRegisterButtonClick() }
        binding.registerEmail.doOnTextChanged { _, _, _, _ ->
            if (!isEmailValid()) {
                binding.registerEmailLayout.error = getString(R.string.message_wrong_email)
            } else {
                binding.registerEmailLayout.error = null
            }
            checkButtonState()
        }
        binding.registerName.doOnTextChanged { _, _, _, _ ->
            if (!isNameValid()) {
                binding.registerNameLayout.error = getString(R.string.message_empty_name)
            } else {
                binding.registerNameLayout.error = null
            }
            checkButtonState()
        }
        binding.registerPassword.doOnTextChanged { _, _, _, _ ->
            if (!isPasswordValid()) {
                binding.registerPasswordLayout.error = getString(R.string.message_wrong_password)
            } else {
                binding.registerPasswordLayout.error = null
            }
            checkButtonState()
        }
        binding.registerPassword2.doOnTextChanged { _, _, _, _ ->
            if (!isPasswordValid()) {
                binding.registerPasswordLayout.error = getString(R.string.message_wrong_password)
            } else {
                binding.registerPasswordLayout.error = null
            }
            checkButtonState()
        }
    }

    private fun isPasswordValid(): Boolean {
        return password.isNotEmpty() && password == password2
    }

    private fun isNameValid(): Boolean {
        return name.isNotEmpty()
    }

    private fun isEmailValid(): Boolean {
        return email.isNotEmpty() && ValidationUtils.isEmail(email)
    }

    private fun checkButtonState() {
        binding.buttonRegister.enable(isEmailValid() && isNameValid() && isPasswordValid())
    }

    private fun handleRegisterButtonClick() {
        viewModel.login(RegisterRequest(email, name, password, password2))
            .observe(this@RegisterActivity) { registerStateHandler.handle(it) }
    }

    private fun prepareRegisterStateHandler(): LoadingStateHandler<LoginResponse> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<LoginResponse> {
            override fun onException(th: Throwable) {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: LoginResponse) {
                localStorageService.login(data.token, data.id)
                MainActivity.start(this@RegisterActivity)
                finishAffinity()
            }

            override fun onInProgress() {
                binding.root.showProgressSpinner(this@RegisterActivity)
            }

            override fun onHttpException(exception: HttpException) {
                if (exception.code() == 409) {
                    this@RegisterActivity.toast(R.string.message_register_email_exists)
                } else {
                    super.onHttpException(exception)
                }
            }
        }
        )
    }

    private val email: String
        get() {
            return binding.registerEmail.text.toString()
        }

    private val name: String
        get() {
            return binding.registerName.text.toString()
        }

    private val password: String
        get() {
            return binding.registerPassword.text.toString()
        }

    private val password2: String
        get() {
            return binding.registerPassword2.text.toString()
        }

}