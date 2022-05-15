package pl.szczeliniak.kitchenassistant.android.ui.activities.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityLoginBinding
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.LoginRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.LoginResponse
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.ui.activities.main.MainActivity
import pl.szczeliniak.kitchenassistant.android.ui.activities.register.RegisterActivity
import pl.szczeliniak.kitchenassistant.android.ui.components.buttons.KaButton
import pl.szczeliniak.kitchenassistant.android.ui.components.forms.KaInput
import pl.szczeliniak.kitchenassistant.android.ui.utils.ContextUtils.Companion.toast
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import pl.szczeliniak.kitchenassistant.android.utils.ValidationUtils
import retrofit2.HttpException
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityLoginBinding
    private val loginStateHandler: LoadingStateHandler<LoginResponse> = prepareLoginStateHandler()

    private val viewModel: LoginActivityViewModel by viewModels()

    @Inject
    lateinit var localStorageService: LocalStorageService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (localStorageService.isLoggedIn()) {
            goToMainActivity()
            finish()
            return
        }

        initLayout()

        checkButtonState()
    }

    private fun initLayout() {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonLogin.onClick = KaButton.OnClick { handleLoginButtonClick() }
        binding.buttonRegister.onClick = KaButton.OnClick {
            RegisterActivity.start(this@LoginActivity)
        }

        binding.loginEmail.onTextChangedValidator = KaInput.OnTextChangedValidator {
            var id: Int? = null
            if (!isEmailValid()) {
                id = R.string.message_wrong_email
            }
            checkButtonState()
            return@OnTextChangedValidator id
        }

        binding.loginPassword.onTextChangedValidator = KaInput.OnTextChangedValidator {
            var id: Int? = null
            if (!isPasswordValid()) {
                id = R.string.message_wrong_password
            }
            checkButtonState()
            return@OnTextChangedValidator id
        }
    }

    private fun isPasswordValid(): Boolean {
        return password.isNotEmpty()
    }

    private fun isEmailValid(): Boolean {
        return email.isNotEmpty() && ValidationUtils.isEmail(email)
    }

    private fun checkButtonState() {
        binding.buttonLogin.enable(isEmailValid() && isPasswordValid())
    }

    private fun goToMainActivity() {
        MainActivity.start(this)
        finish()
    }

    private fun handleLoginButtonClick() {
        viewModel.login(LoginRequest(email, password))
            .observe(this@LoginActivity) { loginStateHandler.handle(it) }
    }

    private fun prepareLoginStateHandler(): LoadingStateHandler<LoginResponse> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<LoginResponse> {
            override fun onException(th: Throwable) {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: LoginResponse) {
                localStorageService.login(data.token, data.id)
                goToMainActivity()
            }

            override fun onInProgress() {
                binding.root.showProgressSpinner(this@LoginActivity)
            }

            override fun onHttpException(exception: HttpException) {
                if (exception.code() == 404 || exception.code() == 400) {
                    this@LoginActivity.toast(R.string.message_login_data_does_not_match)
                    binding.loginPassword.text = ""
                } else {
                    super.onHttpException(exception)
                }
            }
        }
        )
    }

    private val email: String
        get() {
            return binding.loginEmail.text
        }

    private val password: String
        get() {
            return binding.loginPassword.text
        }

}