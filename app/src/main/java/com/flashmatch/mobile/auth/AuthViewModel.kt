package com.flashmatch.mobile.auth

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val googleSignInClient: GoogleSignInClient

    private val _user = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        val clientId = application.getString(com.flashmatch.mobile.R.string.default_web_client_id)
        val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
        if (clientId.isNotBlank()) {
            gsoBuilder.requestIdToken(clientId)
        }
        googleSignInClient = GoogleSignIn.getClient(application, gsoBuilder.build())
    }

    fun getSignInIntent() = googleSignInClient.signInIntent

    fun handleSignInResult(account: GoogleSignInAccount?) {
        if (account == null) {
            _error.value = "Sign-in cancelled"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                val result = auth.signInWithCredential(credential).await()
                _user.value = result.user
            } catch (e: Exception) {
                _error.value = e.message ?: "Sign-in failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInWithGitHub(activity: Activity) {
        val provider = OAuthProvider.newBuilder("github.com").build()
        _isLoading.value = true
        _error.value = null

        val pending = auth.pendingAuthResult
        if (pending != null) {
            pending
                .addOnSuccessListener { result -> _user.value = result.user; _isLoading.value = false }
                .addOnFailureListener { e -> _error.value = e.message ?: "GitHub sign-in failed"; _isLoading.value = false }
            return
        }

        auth.startActivityForSignInWithProvider(activity, provider)
            .addOnSuccessListener { result -> _user.value = result.user; _isLoading.value = false }
            .addOnFailureListener { e -> _error.value = e.message ?: "GitHub sign-in failed"; _isLoading.value = false }
    }

    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
        _user.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
