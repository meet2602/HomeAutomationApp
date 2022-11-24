package com.home.automation

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.home.automation.databinding.ActivityRegisterBinding
import com.home.automation.models.UserModel
import com.home.automation.utils.*
import com.home.automation.viewmodel.DeviceViewModel
import com.home.automation.viewmodel.UserViewModel


class RegisterActivity : AppCompatActivity(), View.OnClickListener {

    private val registerBinding: ActivityRegisterBinding by lazy {
        DataBindingUtil.setContentView(
            this,
            R.layout.activity_register
        )
    }
    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
            .create(UserViewModel()::class.java)
    }
    private val deviceViewModel: DeviceViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
            .create(DeviceViewModel()::class.java)
    }
    private val sessionManager: SessionManager by lazy { SessionManager(this) }
    private var isValidPassword = false
    private var isValidConPassword = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerBinding.noInternet.noInternetClicks = this

        registerBinding.txtLogin.setOnClickListener {
            hideKeyBoard(this)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        registerBinding.edPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditTextPassword(
                    registerBinding.edPassword,
                    registerBinding.txtPasswordL,
                    "password"
                )
            }
        })
        registerBinding.edConPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditTextPassword(
                    registerBinding.edConPassword,
                    registerBinding.txtConPasswordL,
                    "conPassword"
                )
            }
        })
        registerBinding.signUpBtn.setOnClickListener {
            validation()
            hideKeyBoard(this)
        }
    }

    private fun validation() {
        if (registerBinding.edUserName.text.toString().trim().isEmpty()) {
            registerBinding.txtUserNameL.error = "Required"
        } else if (registerBinding.edEmailId.text.toString().trim().isEmpty()) {
            registerBinding.txtUserNameL.error = null
            registerBinding.txtEmailL.error = "Required"
        } else if (!validEmail(registerBinding.edEmailId.text.toString().trim())) {
            registerBinding.txtEmailL.error = "Enter valid e-mail!"
        } else {
            registerBinding.txtEmailL.error = null
            validateEditTextPassword(
                registerBinding.edPassword,
                registerBinding.txtPasswordL,
                "password"
            )
            if (isValidPassword) {
                validateEditTextPassword(
                    registerBinding.edConPassword,
                    registerBinding.txtConPasswordL,
                    "conPassword"
                )
                if (isValidConPassword) {
                    if (registerBinding.edPassword.text.toString()
                            .trim() != registerBinding.edConPassword.text.toString().trim()
                    ) {
                        registerBinding.txtConPasswordL.error = "Password don't match!"
                    } else {
                        registerBinding.txtConPasswordL.error = null
                        val name = registerBinding.edUserName.text.toString().trim()
                        val email = registerBinding.edEmailId.text.toString().trim()
                        val password = registerBinding.edPassword.text.toString().trim()
                        callRegister(name, email, password)
                    }
                }
            }
        }
    }

    private fun callRegister(name: String, email: String, password: String) {
        userViewModel.createAccount(email, password).observe(this) {
            when (it.status) {
                Status.LOADING -> {

                }
                Status.SUCCESS -> {
                    Log.d("success", it.message.toString())
                    callRegisterUser(name, email)
                }
                Status.ERROR -> {
                    Log.d("error", it.message.toString())
                    longShowToast(it.message.toString())
                }
            }

        }
    }

    private fun callRegisterUser(name: String, email: String) {
        val userModel = UserModel(
            FirebaseAuth.getInstance().uid!!,
            name, email, "", false
        )
        userViewModel.registerUser(FirebaseAuth.getInstance().uid!!, userModel).observe(this) {
            when (it.status) {
                Status.LOADING -> {

                }
                Status.SUCCESS -> {
                    Log.d("success", it.message.toString())

                    sessionManager.createAccount(
                        FirebaseAuth.getInstance().uid!!,
                        name,
                        email
                    )
                    callAddComponents()
                }
                Status.ERROR -> {
                    Log.d("error", it.message.toString())
                    longShowToast(it.message.toString())
                }
            }

        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            registerBinding.noInternet.tvRetry.id -> {
                if (isOnline()) {
                    gone(registerBinding.noInternet.llNoInternet)
                    visible(registerBinding.rootLayout)
                } else {
                    visible(registerBinding.noInternet.llNoInternet)
                    gone(registerBinding.rootLayout)
                }
            }
        }
    }

    private fun validateEditTextPassword(
        editText: EditText,
        textInputLayout: TextInputLayout,
        state: String,
    ) {
        var truthState = false
        if (editText.text.toString().trim().isEmpty()) {
            textInputLayout.error = "Required"
        } else if (editText.text.toString().length < 8 && editText.text.toString().length < 16) {
            textInputLayout.error = "Password must be 8 to 16 character!"
        } else {
            textInputLayout.error = null
            truthState = true
        }
        if (truthState) {
            if (state == "password") {
                isValidPassword = true
            } else if (state == "conPassword") {
                isValidConPassword = true
            }
        } else {
            if (state == "password") {
                isValidPassword = false
            } else if (state == "conPassword") {
                isValidConPassword = false
            }
        }
    }

    private fun callAddComponents() {
        val body = hashMapOf<String, Any>(
            "led1StateMsg" to "led1Off",
            "led2StateMsg" to "led2Off",
            "led3StateMsg" to "led3Off",
            "led4StateMsg" to "led4Off",
            "fanStateMsg" to "fan0Off",
            "rgbStateMsg" to "RGBOff/0/0/0",
            "tempValue" to 0.0F,
        )

        deviceViewModel.addComponents(sessionManager.userId, body).observe(this) {
            when (it.status) {

                Status.LOADING -> {

                }
                Status.SUCCESS -> {
                    Log.d("success", it.message.toString())
                    longShowToast("Register Successfully!")
                    startActivity(Intent(this, VerifyActivity::class.java))
                    startActivityAnimation()
                    finish()
                }
                Status.ERROR -> {
                    Log.d("error", it.message.toString())
                    longShowToast(it.message.toString())
                }
            }

        }
    }
}