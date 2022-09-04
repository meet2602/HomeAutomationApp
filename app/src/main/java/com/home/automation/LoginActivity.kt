package com.home.automation

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.home.automation.databinding.ActivityLoginBinding
import com.home.automation.utils.*
import com.home.automation.viewmodel.UserViewModel


class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private val loginBinding: ActivityLoginBinding by lazy {
        DataBindingUtil.setContentView(
            this,
            R.layout.activity_login
        )
    }
    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
            .create(UserViewModel()::class.java)
    }
    private val sessionManager: SessionManager by lazy { SessionManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding.noInternet.noInternetClicks = this

        loginBinding.logInBtn.setOnClickListener {
            validation()
            hideKeyBoard(this)
        }
        loginBinding.txtSignUp.setOnClickListener {
            hideKeyBoard(this)
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
        loginBinding.edPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (loginBinding.edPassword.text.toString().trim().isEmpty()) {
                    loginBinding.txtPasswordL.error = "Required"
                } else if (loginBinding.edPassword.text.toString().trim().length < 8) {
                    loginBinding.txtPasswordL.error = "Password must be 8 character!"
                } else {
                    loginBinding.txtPasswordL.error = null
                }
            }
        })
    }

    private fun validation() {
        if (isOnline()) {
            gone(loginBinding.noInternet.llNoInternet)
            visible(loginBinding.rootLayout)
            if (loginBinding.edEmailId.text.toString().trim().isEmpty()) {
                loginBinding.txtEmailL.error = "Required"
            } else if (!validEmail(loginBinding.edEmailId.text.toString().trim())) {
                loginBinding.txtEmailL.error = "Enter valid e-mail!"
            } else if (loginBinding.edPassword.text.toString().trim().isEmpty()) {
                loginBinding.txtEmailL.error = null
                loginBinding.txtPasswordL.error = "Required"
            } else if (loginBinding.edPassword.text.toString().trim().length < 8) {
                loginBinding.txtPasswordL.error = "Password must be 8 character!"
            } else {
                loginBinding.txtPasswordL.error = null
                val email = loginBinding.edEmailId.text.toString().trim()
                val password = loginBinding.edPassword.text.toString().trim()

                callLogin(email, password)

            }
        } else {
            visible(loginBinding.noInternet.llNoInternet)
            gone(loginBinding.rootLayout)
        }
    }


    private fun callLogin(email: String, password: String) {
        userViewModel.signIn(email, password).observe(this) {
            when (it.status) {
                Status.LOADING -> {

                }
                Status.SUCCESS -> {
                    Log.d("success", it.message.toString())
                    callGetUserDetail()
                }
                Status.ERROR -> {
                    Log.d("error", it.message.toString())
                    longShowToast(it.message.toString())
                }
            }

        }
    }

    private fun callGetUserDetail() {
        userViewModel.getUserDetail(FirebaseAuth.getInstance().uid!!).observe(this) {
            when (it.status) {
                Status.LOADING -> {

                }
                Status.SUCCESS -> {
                    Log.d("success", it.message.toString())
                    if (it.data != null) {
                        sessionManager.login(
                            FirebaseAuth.getInstance().uid!!,
                            it.data.userName,
                            it.data.userEmail,
                            it.data.deviceIpAddress,
                            it.data.deviceConnect
                        )
                        longShowToast("Login Successfully!")
                        startActivity(Intent(this, DashBoardActivity::class.java))
                        startActivityAnimation()
                        finish()
                    }
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
            loginBinding.noInternet.tvRetry.id -> {
                if (isOnline()) {
                    gone(loginBinding.noInternet.llNoInternet)
                    visible(loginBinding.rootLayout)
                } else {
                    visible(loginBinding.noInternet.llNoInternet)
                    gone(loginBinding.rootLayout)
                }
            }
        }
    }

}