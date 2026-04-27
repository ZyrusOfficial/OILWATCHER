package com.oilwatcher.monitor.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for Firebase Authentication operations.
 */
interface AuthRepository {
    /** Sign in with email and password. */
    suspend fun login(email: String, password: String): Result<FirebaseUser>

    /** Create a new account with email, password, and display name. */
    suspend fun signUp(name: String, email: String, password: String): Result<FirebaseUser>

    /** Send a password reset email. */
    suspend fun resetPassword(email: String): Result<Unit>

    /** Sign out the current user. */
    fun signOut()

    /** Get the currently authenticated user, or null. */
    fun getCurrentUser(): FirebaseUser?

    /** Check if a user is currently logged in. */
    fun isLoggedIn(): Boolean
}

/**
 * Firebase implementation of [AuthRepository].
 *
 * On sign-up, the user's display name is set via UserProfileChangeRequest,
 * and a minimal user profile document is created in Firestore at `users/{uid}`.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
    }

    override suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            Log.d(TAG, "login: attempting signInWithEmailAndPassword for $email")
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
                ?: return Result.failure(Exception("Login succeeded but user is null."))
            Log.d(TAG, "login: success, uid=${user.uid}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "login: failed — ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun signUp(
        name: String,
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            Log.d(TAG, "signUp: attempting createUserWithEmailAndPassword for $email")
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
                ?: return Result.failure(Exception("Sign-up succeeded but user is null."))
            Log.d(TAG, "signUp: account created, uid=${user.uid}")

            // Set display name on the Firebase Auth profile
            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(profileUpdate).await()
            Log.d(TAG, "signUp: display name set to '$name'")

            // Create a Firestore user document for community features
            // We use addOnCompleteListener instead of await() to prevent 
            // the sign-up flow from hanging if the database isn't fully configured in the console.
            val userDoc = hashMapOf(
                "uid" to user.uid,
                "name" to name,
                "email" to email,
                "contributions" to 0,
                "points" to 0,
                "rank" to 0,
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            firestore.collection("users").document(user.uid).set(userDoc)
                .addOnSuccessListener {
                    Log.d(TAG, "signUp: Firestore user document created")
                }
                .addOnFailureListener { e ->
                    // Log failure, but don't block the user from logging in locally
                    Log.w(TAG, "signUp: Firestore user document creation failed", e)
                }

            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "signUp: failed — ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            Log.d(TAG, "resetPassword: sending to $email")
            firebaseAuth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "resetPassword: success")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "resetPassword: failed — ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(mapAuthException(e))
        }
    }

    override fun signOut() {
        Log.d(TAG, "signOut")
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    /**
     * Map Firebase Auth exceptions to user-friendly messages.
     *
     * We check both the FirebaseAuthException error code (when available)
     * and the exception message string, because different Firebase SDK
     * versions may use one or the other.
     */
    private fun mapAuthException(e: Exception): Exception {
        val rawMsg = e.message ?: ""
        val errorCode = (e as? FirebaseAuthException)?.errorCode ?: ""

        Log.d(TAG, "mapAuthException: errorCode='$errorCode', rawMsg='$rawMsg'")

        val message = when {
            // ── Configuration not found (Email/Password auth not enabled in Firebase Console) ──
            rawMsg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ||
            rawMsg.contains("configuration not found", ignoreCase = true) ->
                "Email/Password sign-in is not enabled. Please enable it in the Firebase Console under Authentication → Sign-in method."

            // ── Internal error (generic Firebase server error) ──
            rawMsg.contains("INTERNAL_ERROR", ignoreCase = true) ||
            rawMsg.contains("An internal error has occurred", ignoreCase = true) ->
                "Firebase server error. Please check that Email/Password authentication is enabled in your Firebase Console, then try again."

            // ── Invalid credentials (modern Firebase SDK lumps wrong password
            //    and user-not-found into this) ──
            errorCode == "ERROR_INVALID_CREDENTIAL" ||
            rawMsg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
            rawMsg.contains("INVALID_CREDENTIAL", ignoreCase = true) ->
                "Invalid email or password. Please check your credentials and try again."

            // ── Invalid email format ──
            errorCode == "ERROR_INVALID_EMAIL" ||
            rawMsg.contains("INVALID_EMAIL", ignoreCase = true) ->
                "The email address is not valid. Please check and try again."

            // ── No account found (older Firebase SDKs) ──
            errorCode == "ERROR_USER_NOT_FOUND" ||
            rawMsg.contains("USER_NOT_FOUND", ignoreCase = true) ||
            rawMsg.contains("no user record", ignoreCase = true) ->
                "No account found with this email. Please sign up first."

            // ── Wrong password (older Firebase SDKs) ──
            errorCode == "ERROR_WRONG_PASSWORD" ||
            rawMsg.contains("WRONG_PASSWORD", ignoreCase = true) ->
                "Incorrect password. Please try again."

            // ── Account already exists ──
            errorCode == "ERROR_EMAIL_ALREADY_IN_USE" ||
            rawMsg.contains("EMAIL_ALREADY_IN_USE", ignoreCase = true) ->
                "An account with this email already exists. Please sign in instead."

            // ── Weak password ──
            errorCode == "ERROR_WEAK_PASSWORD" ||
            rawMsg.contains("WEAK_PASSWORD", ignoreCase = true) ->
                "Password is too weak. Please use at least 6 characters."

            // ── User disabled ──
            errorCode == "ERROR_USER_DISABLED" ||
            rawMsg.contains("USER_DISABLED", ignoreCase = true) ->
                "This account has been disabled. Please contact support."

            // ── Rate limiting ──
            errorCode == "ERROR_TOO_MANY_REQUESTS" ||
            rawMsg.contains("TOO_MANY_REQUESTS", ignoreCase = true) ||
            rawMsg.contains("BLOCKING_FUNCTION_ERROR_RESPONSE", ignoreCase = true) ->
                "Too many attempts. Please wait a moment and try again."

            // ── Network errors ──
            rawMsg.contains("NETWORK", ignoreCase = true) ||
            rawMsg.contains("network error", ignoreCase = true) ||
            rawMsg.contains("Unable to resolve host", ignoreCase = true) ||
            rawMsg.contains("timeout", ignoreCase = true) ->
                "Network error. Please check your internet connection and try again."

            // ── Operation not allowed (e.g. email/password sign-in disabled) ──
            errorCode == "ERROR_OPERATION_NOT_ALLOWED" ||
            rawMsg.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) ->
                "Email/Password sign-in is not enabled. Please enable it in the Firebase Console under Authentication → Sign-in method."

            // ── Fallback: show the raw message for unknown errors ──
            else -> "Something went wrong: ${rawMsg.take(150)}"
        }
        return Exception(message)
    }
}

