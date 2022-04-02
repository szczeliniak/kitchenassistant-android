package pl.szczeliniak.kitchenassistant.android.ui.activities.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityLoginBinding
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.LoginRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.LoginResponse
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.ui.activities.main.MainActivity
import pl.szczeliniak.kitchenassistant.android.ui.utils.enable
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.showProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.toast
import pl.szczeliniak.kitchenassistant.android.utils.ValidationUtils
import retrofit2.HttpException
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class LoginActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginStateHandler: LoadingStateHandler<LoginResponse>

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

        binding = ActivityLoginBinding.inflate(layoutInflater)
        binding.activityLoginForm.activityLoginButtonLogin.setOnClickListener { handleLoginButtonClick() }
        setContentView(binding.root)

        loginStateHandler = prepareLoginStateHandler()
    }

    private fun goToMainActivity() {
        MainActivity.start(this)
        finish()
    }

    private fun handleLoginButtonClick() {
        val email = binding.activityLoginForm.activityLoginEdittextEmail.text.toString()
        val password = binding.activityLoginForm.activityLoginEdittextPassword.text.toString()

        if (email.isEmpty() || !ValidationUtils.isEmail(email)) {
            toast(R.string.activity_login_error_wrong_email)
        } else if (password.isEmpty()) {
            toast(R.string.activity_login_error_wrong_password)
        } else {
            viewModel.login(LoginRequest(email, password))
                .observe(this@LoginActivity) { loginStateHandler.handle(it) }
        }
    }

    private fun prepareLoginStateHandler(): LoadingStateHandler<LoginResponse> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<LoginResponse> {
            override fun onException(th: Throwable) {
                binding.activityLoginLayout!!.hideProgressSpinner(this@LoginActivity)
                binding.activityLoginForm.activityLoginButtonLogin.enable(true)
            }

            override fun onSuccess(data: LoginResponse) {
                handleLoginSuccess(data)
            }

            override fun onInProgress() {
                binding.activityLoginLayout!!.showProgressSpinner(this@LoginActivity)
                binding.activityLoginForm.activityLoginButtonLogin.enable(false)

            }

            override fun onHttpException(exception: HttpException) {
                if (exception.code() == 404 || exception.code() == 400) {
                    this@LoginActivity.toast(R.string.activity_login_error_login_data_does_not_match)
                }
                binding.activityLoginForm.activityLoginEdittextPassword.setText("")
                onException(exception)
            }
        }
        )
    }

    private fun handleLoginSuccess(response: LoginResponse) {
        localStorageService.login(response.token, response.id)
        goToMainActivity()
    }

}