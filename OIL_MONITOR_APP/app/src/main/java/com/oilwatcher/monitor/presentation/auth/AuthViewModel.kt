package com.oilwatcher.monitor.presentation.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.oilwatcher.monitor.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * Authentication state for the app.
 */
sealed class AuthState {
    /** Initial state — checking if user is already logged in. */
    data object Loading : AuthState()

    /** User is authenticated. */
    data class Authenticated(val user: FirebaseUser) : AuthState()

    /** User is not authenticated. */
    data object Unauthenticated : AuthState()
}

/**
 * ViewModel managing authentication flows.
 *
 * Injected via Hilt. Used by AppNavHost to determine start destination,
 * and by AuthBridge to execute login/signup/reset operations.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /** Callback for WebView result delivery. */
    private var _authResultCallback: ((Boolean, String) -> Unit)? = null

    /**
     * Monotonically increasing token used to track which AuthBridge instance
     * "owns" the current callback. This prevents a stale `cleanup()` call
     * (from a previous screen's DisposableEffect.onDispose) from wiping the
     * callback that a newer AuthBridge just registered.
     */
    private val _callbackToken = AtomicInteger(0)

    init {
        checkAuthState()
    }

    /**
     * Check if the user is already logged in (e.g., on app launch).
     */
    fun checkAuthState() {
        val user = authRepository.getCurrentUser()
        _authState.value = if (user != null) {
            AuthState.Authenticated(user)
        } else {
            AuthState.Unauthenticated
        }
    }

    /**
     * Set a callback that will be invoked with (success, message) when an
     * auth operation completes. The AuthBridge uses this to relay results
     * back to the WebView JavaScript via evaluateJavascript.
     *
     * Returns a token that must be passed to [clearAuthResultCallback] so
     * that only the owning AuthBridge can clear its own callback.
     */
    fun setAuthResultCallback(callback: (Boolean, String) -> Unit): Int {
        val token = _callbackToken.incrementAndGet()
        _authResultCallback = callback
        Log.d(TAG, "setAuthResultCallback: token=$token")
        return token
    }

    /**
     * Clear the callback **only if** the caller still owns it (i.e. its
     * token matches the current generation). This prevents a previous
     * screen's `DisposableEffect.onDispose` from nuking the new screen's
     * callback.
     */
    fun clearAuthResultCallback(token: Int) {
        if (_callbackToken.get() == token) {
            Log.d(TAG, "clearAuthResultCallback: cleared (token=$token)")
            _authResultCallback = null
        } else {
            Log.d(TAG, "clearAuthResultCallback: skipped stale token=$token, current=${_callbackToken.get()}")
        }
    }

    /**
     * Email/password login.
     */
    fun login(email: String, password: String) {
        Log.d(TAG, "login called: email='$email', pwLen=${password.length}")
        Log.d(TAG, "login: callback is ${if (_authResultCallback != null) "SET" else "NULL"}")

        // Client-side validation
        if (email.isBlank() || password.isBlank()) {
            Log.d(TAG, "login: VALIDATION FAIL - empty fields")
            _authResultCallback?.invoke(false, "Please fill in all fields.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.d(TAG, "login: VALIDATION FAIL - invalid email format")
            _authResultCallback?.invoke(false, "Please enter a valid email address.")
            return
        }

        Log.d(TAG, "login: validation passed, launching coroutine...")
        viewModelScope.launch {
            Log.d(TAG, "login: coroutine started, calling authRepository.login()...")
            try {
                val result = authRepository.login(email, password)
                Log.d(TAG, "login: authRepository.login() returned, isSuccess=${result.isSuccess}")
                result.fold(
                    onSuccess = { user ->
                        Log.d(TAG, "login: SUCCESS for ${user.email}")
                        _authState.value = AuthState.Authenticated(user)
                        Log.d(TAG, "login: callback is ${if (_authResultCallback != null) "SET" else "NULL"} before invoke")
                        _authResultCallback?.invoke(true, "Welcome back!")
                        Log.d(TAG, "login: callback invoked with success")
                    },
                    onFailure = { error ->
                        Log.e(TAG, "login: FAILURE - ${error.message}", error)
                        Log.d(TAG, "login: callback is ${if (_authResultCallback != null) "SET" else "NULL"} before invoke")
                        _authResultCallback?.invoke(false, error.message ?: "Login failed. Please try again.")
                        Log.d(TAG, "login: callback invoked with error")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "login: UNEXPECTED EXCEPTION", e)
                _authResultCallback?.invoke(false, "An unexpected error occurred: ${e.message}")
            }
        }
    }

    /**
     * Email/password sign-up with display name.
     */
    fun signUp(name: String, email: String, password: String) {
        Log.d(TAG, "signUp called: name='$name', email='$email', pwLen=${password.length}")
        Log.d(TAG, "signUp: callback is ${if (_authResultCallback != null) "SET" else "NULL"}")

        // Client-side validation
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            Log.d(TAG, "signUp: VALIDATION FAIL - empty fields")
            _authResultCallback?.invoke(false, "Please fill in all fields.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.d(TAG, "signUp: VALIDATION FAIL - invalid email format")
            _authResultCallback?.invoke(false, "Please enter a valid email address.")
            return
        }
        if (password.length < 6) {
            Log.d(TAG, "signUp: VALIDATION FAIL - password too short (${password.length})")
            _authResultCallback?.invoke(false, "Password must be at least 6 characters.")
            return
        }

        Log.d(TAG, "signUp: validation passed, launching coroutine...")
        viewModelScope.launch {
            Log.d(TAG, "signUp: coroutine started, calling authRepository.signUp()...")
            try {
                val result = authRepository.signUp(name, email, password)
                Log.d(TAG, "signUp: authRepository.signUp() returned, isSuccess=${result.isSuccess}")
                result.fold(
                    onSuccess = { user ->
                        Log.d(TAG, "signUp: SUCCESS for ${user.email}, uid=${user.uid}")
                        _authState.value = AuthState.Authenticated(user)
                        Log.d(TAG, "signUp: callback is ${if (_authResultCallback != null) "SET" else "NULL"} before invoke")
                        _authResultCallback?.invoke(true, "Account created successfully!")
                        Log.d(TAG, "signUp: callback invoked with success")
                    },
                    onFailure = { error ->
                        Log.e(TAG, "signUp: FAILURE - ${error.message}", error)
                        Log.d(TAG, "signUp: callback is ${if (_authResultCallback != null) "SET" else "NULL"} before invoke")
                        _authResultCallback?.invoke(false, error.message ?: "Sign-up failed. Please try again.")
                        Log.d(TAG, "signUp: callback invoked with error")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "signUp: UNEXPECTED EXCEPTION", e)
                _authResultCallback?.invoke(false, "An unexpected error occurred: ${e.message}")
            }
        }
    }

    /**
     * Send password reset email.
     */
    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _authResultCallback?.invoke(false, "Please enter your email address.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authResultCallback?.invoke(false, "Please enter a valid email address.")
            return
        }

        Log.d(TAG, "resetPassword: attempting for $email")
        viewModelScope.launch {
            try {
                val result = authRepository.resetPassword(email)
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "resetPassword: success")
                        _authResultCallback?.invoke(true, "Password reset link sent to $email")
                    },
                    onFailure = { error ->
                        Log.e(TAG, "resetPassword: failed", error)
                        _authResultCallback?.invoke(false, error.message ?: "Failed to send reset link.")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "resetPassword: unexpected exception", e)
                _authResultCallback?.invoke(false, "An unexpected error occurred. Please try again.")
            }
        }
    }

    /**
     * Sign out and reset auth state.
     */
    fun signOut() {
        authRepository.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    /**
     * Whether the user is currently logged in.
     */
    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()
}
