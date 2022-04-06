package pl.szczeliniak.kitchenassistant.android.ui.activities.register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityRegisterBinding
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.RegisterRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.LoginResponse
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.ui.activities.main.MainActivity
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.init
import pl.szczeliniak.kitchenassistant.android.ui.utils.showProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.toast
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
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        binding.toolbarLayout.toolbar.init(this, R.string.title_activity_register)
        binding.buttonRegister.setOnClickListener { handleRegisterButtonClick() }
        setContentView(binding.root)
    }

    private fun handleRegisterButtonClick() {
        val email = binding.registerEmail.text.toString()
        val name = binding.registerName.text.toString()
        val password = binding.registerPassword.text.toString()
        val password2 = binding.registerPassword2.text.toString()

        if (name.isEmpty()) {
            toast(R.string.message_empty_name)
        } else if (email.isEmpty() || !ValidationUtils.isEmail(email)) {
            toast(R.string.message_wrong_email)
        } else if (password.isEmpty() || password2.isEmpty() || password != password2) {
            toast(R.string.message_wrong_password)
        } else {
            viewModel.login(RegisterRequest(email, name, password, password2))
                .observe(this@RegisterActivity) { registerStateHandler.handle(it) }
        }
    }

    private fun prepareRegisterStateHandler(): LoadingStateHandler<LoginResponse> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<LoginResponse> {
            override fun onException(th: Throwable) {
                binding.root.hideProgressSpinner(this@RegisterActivity)
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

}