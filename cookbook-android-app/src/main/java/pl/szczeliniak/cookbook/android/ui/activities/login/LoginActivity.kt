package pl.szczeliniak.cookbook.android.ui.activities.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import dagger.hilt.android.AndroidEntryPoint
import pl.szczeliniak.cookbook.android.R
import pl.szczeliniak.cookbook.android.databinding.ActivityLoginBinding
import pl.szczeliniak.cookbook.android.network.LoadingStateHandler
import pl.szczeliniak.cookbook.android.network.requests.LoginRequest
import pl.szczeliniak.cookbook.android.network.requests.LoginWithFacebookRequest
import pl.szczeliniak.cookbook.android.network.responses.LoginResponse
import pl.szczeliniak.cookbook.android.services.LocalStorageService
import pl.szczeliniak.cookbook.android.ui.activities.main.MainActivity
import pl.szczeliniak.cookbook.android.ui.utils.ButtonUtils.Companion.enable
import pl.szczeliniak.cookbook.android.ui.utils.ContextUtils.Companion.toast
import pl.szczeliniak.cookbook.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.cookbook.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import pl.szczeliniak.cookbook.android.utils.ValidationUtils
import retrofit2.HttpException
import java.time.LocalDate
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
    private val refreshTokenStateHandler: LoadingStateHandler<LoginResponse> = prepareLoginStateHandler()

    private val viewModel: LoginActivityViewModel by viewModels()
    private val facebookCallbackManager: CallbackManager = CallbackManager.Factory.create()

    @Inject
    lateinit var localStorageService: LocalStorageService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val refreshExpDate = localStorageService.getRefreshExpirationDate()
        if (refreshExpDate != null && refreshExpDate.isAfter(LocalDate.now())) {
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

        binding.buttonLoginWithFacebook.setOnClickListener {
            val loginManager = LoginManager.getInstance()
            loginManager.registerCallback(facebookCallbackManager, object : FacebookCallback<LoginResult> {
                override fun onCancel() {}

                override fun onError(error: FacebookException) {
                    toast(R.string.message_login_with_facebook_error)
                }

                override fun onSuccess(result: LoginResult) {
                    viewModel.loginWithFacebook(LoginWithFacebookRequest(result.accessToken.token))
                        .observe(this@LoginActivity) { loginStateHandler.handle(it) }
                }
            })
            loginManager.logInWithReadPermissions(this, listOf("public_profile", "email"))
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
                localStorageService.login(data.accessToken, data.refreshToken)
                goToMainActivity()
            }

            override fun onInProgress() {
                binding.root.showProgressSpinner(this@LoginActivity)
            }

            override fun onHttpException(exception: HttpException) {
                if (exception.code() == 404 || exception.code() == 400) {
                    this@LoginActivity.toast(R.string.message_login_data_does_not_match)
                    binding.loginPassword.setText("")
                } else if (exception.code() == 403) {
                    this@LoginActivity.toast(R.string.message_token_expired)
                    localStorageService.logout()
                    LoginManager.getInstance().logOut()
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

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

}