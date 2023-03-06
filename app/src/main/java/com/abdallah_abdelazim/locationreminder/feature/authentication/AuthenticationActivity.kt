package com.abdallah_abdelazim.locationreminder.feature.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.abdallah_abdelazim.locationreminder.R
import com.abdallah_abdelazim.locationreminder.databinding.ActivityAuthenticationBinding
import com.abdallah_abdelazim.locationreminder.feature.reminders.RemindersActivity
import com.abdallah_abdelazim.locationreminder.utils.fadeIn
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * The starting point of the app, It asks the users to sign-in / register, and redirects the
 * signed-in users to the [RemindersActivity].
 */
class AuthenticationActivity : AppCompatActivity() {

    private val viewModel: AuthenticationViewModel by viewModel()

    private lateinit var binding: ActivityAuthenticationBinding

    private lateinit var authUiResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_authentication
        )

        registerAuthUiResult()

        setupUiListeners()

        listenAuthStatus()
    }

    private fun listenAuthStatus() {
        lifecycleScope.launchWhenCreated {
            viewModel.authStatusFlow.collect { authStatus ->
                when (authStatus) {
                    AuthenticationStatus.LOGGED_IN -> {
                        Log.d(
                            TAG,
                            "User is logged in"
                        )

                        val intent =
                            Intent(this@AuthenticationActivity, RemindersActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    AuthenticationStatus.LOGGED_OUT -> {
                        Log.d(
                            TAG,
                            "User is logged out"
                        )
                        binding.btnLogin.fadeIn()
                    }
                }
            }
        }
    }

    private fun setupUiListeners() {
        binding.btnLogin.setOnClickListener {
            signInUser()
        }
    }

    private fun signInUser() {
        authUiResultLauncher.launch(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(
                    listOf(
                        AuthUI.IdpConfig.EmailBuilder().build(),
                        AuthUI.IdpConfig.GoogleBuilder().build()
                    )
                )
                .setTheme(R.style.Theme_LocationReminder)
                .setLogo(R.drawable.ic_app_logo)
                .build()
        )
    }

    private fun registerAuthUiResult() {
        authUiResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            val response = IdpResponse.fromResultIntent(result.data)
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(
                    TAG,
                    "Login success - user: ${FirebaseAuth.getInstance().currentUser?.displayName}"
                )
            } else {
                Log.e(
                    TAG,
                    "Login failed - error: ${response?.error?.errorCode}"
                )
                Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {

        private val TAG = AuthenticationActivity::class.simpleName

    }

}
