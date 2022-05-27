package pl.szczeliniak.kitchenassistant.android.ui.activities.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import dagger.hilt.android.AndroidEntryPoint
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityLoginBinding
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.LoginRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.LoginResponse
import pl.szczeliniak.kitchenassistant.android.network.responses.RefreshTokenResponse
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.ui.activities.main.MainActivity
import pl.szczeliniak.kitchenassistant.android.ui.activities.register.RegisterActivity
import pl.szczeliniak.kitchenassistant.android.ui.utils.ButtonUtils.Companion.enable
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
    private val refreshTokenStateHandler: LoadingStateHandler<RefreshTokenResponse> = prepareRefreshTokenStateHandler()

    private val viewModel: LoginActivityViewModel by viewModels()

    @Inject
    lateinit var localStorageService: LocalStorageService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (localStorageService.isLoggedIn()) {
            refreshToken()
        }

        initLayout()

        checkButtonState()
    }

    private fun refreshToken() {
        viewModel.refreshToken().observe(this) { refreshTokenStateHandler.handle(it) }
    }

    private fun initLayout() {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonLogin.setOnClickListener { handleLoginButtonClick() }
        binding.buttonRegister.setOnClickListener {
            RegisterActivity.start(this@LoginActivity)
        }

        binding.loginEmail.doOnTextChanged { _, _, _, _ ->
            if (!isEmailValid()) {
                binding.loginEmailLayout.error = getString(R.string.message_wrong_email)
            } else {
                binding.loginEmailLayout.error = null
            }
            checkButtonState()
        }

        binding.loginPassword.doOnTextChanged { _, _, _, _ ->
            if (!isPasswordValid()) {
                binding.loginPasswordLayout.error = getString(R.string.message_wrong_password)
            } else {
                binding.loginPasswordLayout.error = null
            }
            checkButtonState()
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
                localStorageService.login(data.token, data.id, data.validTo)
                goToMainActivity()
            }

            override fun onInProgress() {
                binding.root.showProgressSpinner(this@LoginActivity)
            }

            override fun onHttpException(exception: HttpException) {
                if (exception.code() == 404 || exception.code() == 400) {
                    this@LoginActivity.toast(R.string.message_login_data_does_not_match)
                    binding.loginPassword.setText("")
                } else {
                    super.onHttpException(exception)
                }
            }
        }
        )
    }

    private fun prepareRefreshTokenStateHandler(): LoadingStateHandler<RefreshTokenResponse> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<RefreshTokenResponse> {
            override fun onException(th: Throwable) {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: RefreshTokenResponse) {
                localStorageService.login(data.token, localStorageService.getId(), data.validTo)
                goToMainActivity()
            }

            override fun onInProgress() {
                binding.root.showProgressSpinner(this@LoginActivity)
            }

            override fun onHttpException(exception: HttpException) {
                if (exception.code() == 403) {
                    this@LoginActivity.toast(R.string.message_token_expired)
                    localStorageService.logout()
                } else {
                    super.onHttpException(exception)
                }
            }
        }
        )
    }

    private val email: String
        get() {
            return binding.loginEmail.text.toString()
        }

    private val password: String
        get() {
            return binding.loginPassword.text.toString()
        }

}