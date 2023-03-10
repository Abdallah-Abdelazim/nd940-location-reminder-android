package com.abdallah_abdelazim.locationreminder.feature.authentication

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class AuthenticationViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    val authStatusFlow = callbackFlow {

        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(
                if (firebaseAuth.currentUser != null)
                    AuthenticationStatus.LOGGED_IN
                else AuthenticationStatus.LOGGED_OUT
            )
        }

        firebaseAuth.addAuthStateListener(authStateListener)

        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }.distinctUntilChanged()

}