package pl.szczeliniak.kitchenassistant.android.ui.activities.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityLoginBinding
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.LoginRequest
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
    private lateinit var loginStateHandler: LoadingStateHandler<Boolean>

    private val viewModel: LoginActivityViewModel by viewModels()

    @Inject
    lateinit var localStorageService: LocalStorageService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (localStorageService.isLoggedIn()) {
            handleLoginSuccess()
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        binding.loginFromLayout.activityLoginButtonLogin.setOnClickListener { handleLoginButtonClick() }
        setContentView(binding.root)

        loginStateHandler = prepareLoginStateHandler()
    }

    private fun handleLoginSuccess() {
        MainActivity.start(this)
        finish()
    }

    private fun handleLoginButtonClick() {
        val email = binding.loginFromLayout.activityLoginInputEmail.text.toString()
        val password = binding.loginFromLayout.activityLoginInputPassword.text.toString()

        if (email.isEmpty() || !ValidationUtils.isEmail(email)) {
            toast(R.string.activity_login_error_wrong_email)
        } else if (password.isEmpty()) {
            toast(R.string.activity_login_error_wrong_password)
        } else {
            viewModel.login(LoginRequest(email, password))
                .observe(this@LoginActivity) { loginStateHandler.handle(it) }
        }
    }

    private fun prepareLoginStateHandler(): LoadingStateHandler<Boolean> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<Boolean> {
            override fun onException(th: Throwable) {
                (binding.root as ViewGroup).hideProgressSpinner(this@LoginActivity)
                binding.loginFromLayout.activityLoginButtonLogin.enable(true)
            }

            override fun onSuccess(data: Boolean) {
                handleLoginSuccess()
            }

            override fun onInProgress() {
                (binding.root as ViewGroup).showProgressSpinner(this@LoginActivity)
                binding.loginFromLayout.activityLoginButtonLogin.enable(false)

            }

            override fun onHttpException(exception: HttpException) {
                if (exception.code() == 404 || exception.code() == 400) {
                    this@LoginActivity.toast(R.string.activity_login_error_login_data_does_not_match)
                }
                binding.loginFromLayout.activityLoginInputPassword.setText("")
                onException(exception)
            }
        }
        )
    }

}