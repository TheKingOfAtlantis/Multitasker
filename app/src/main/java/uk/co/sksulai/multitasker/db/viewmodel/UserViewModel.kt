package uk.co.sksulai.multitasker.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuthException
import uk.co.sksulai.multitasker.db.repo.UserRepository

typealias emailError = suspend (emailError: String, passwordError: String, authError: String) -> Unit

class UserViewModel(app: Application) : AndroidViewModel(app) {
    private val userRepo by lazy { UserRepository(app) }

    val currentUser = userRepo.currentUser

    private suspend fun handleAuthError(
        err: FirebaseAuthException,
        onError: emailError
    ) {
        // Copied from StackOverflow: https://stackoverflow.com/a/48503254/3856359
        val authError = when (err.errorCode) {
            "ERROR_INVALID_CUSTOM_TOKEN"                     -> "The custom token format is incorrect. Please check the documentation."
            "ERROR_CUSTOM_TOKEN_MISMATCH"                    -> "The custom token corresponds to a different audience."
            "ERROR_INVALID_CREDENTIAL"                       -> "The supplied auth credential is malformed or has expired."
            "ERROR_INVALID_EMAIL"                            -> "The email address is badly formatted."
            "ERROR_WRONG_PASSWORD"                           -> "The password is invalid or the user does not have a password."
            "ERROR_USER_MISMATCH"                            -> "The supplied credentials do not correspond to the previously signed in user."
            "ERROR_REQUIRES_RECENT_LOGIN"                    -> "This operation is sensitive and requires recent authentication. Log in again before retrying this request."
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> "An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address."
            "ERROR_EMAIL_ALREADY_IN_USE"                     -> "The email address is already in use by another account."
            "ERROR_CREDENTIAL_ALREADY_IN_USE"                -> "This credential is already associated with a different user account."
            "ERROR_USER_DISABLED"                            -> "The user account has been disabled by an administrator."
            "ERROR_USER_TOKEN_EXPIRED"                       -> "The user\'s credential is no longer valid. The user must sign in again."
            "ERROR_USER_NOT_FOUND"                           -> "There is no user record corresponding to this identifier. The user may have been deleted."
            "ERROR_INVALID_USER_TOKEN"                       -> "The user\'s credential is no longer valid. The user must sign in again."
            "ERROR_OPERATION_NOT_ALLOWED"                    -> "This operation is not allowed. You must enable this service in the console."
            "ERROR_WEAK_PASSWORD"                            -> "The password is invalid it must 6 characters at least"
            else -> throw err // We couldn't provide user friendly explanation for exception (Must be something fatal or something new)
        }
        // Some error codes should be given as email or password errors instead
        when(err.errorCode) {
            "ERROR_INVALID_EMAIL",
            "ERROR_EMAIL_ALREADY_IN_USE" -> onError(authError, "", "")
            "ERROR_WRONG_PASSWORD",
            "ERROR_WEAK_PASSWORD" -> onError("", authError, "")
            else -> onError("", "", authError)
        }
    }

    suspend fun authenticate(email: String, password: String, onError: suspend (emailError: String, passwordError: String, authError: String) -> Unit) = when {
        email.isEmpty()    -> { onError("No email provided", "", "") }
        password.isEmpty() -> { onError("", "No password provided", "") }
        else ->
        try { userRepo.authenticate(email, password) }
        catch (e: FirebaseAuthException) { handleAuthError(e, onError) }
    }
    suspend fun create(email: String, password : String, onError: suspend (emailError: String, passwordError: String, authError: String) -> Unit) = when {
        email.isEmpty()    -> onError("No email provided", "", "")
        password.isEmpty() -> onError("", "No password provided", "")
        else ->
            try { userRepo.create(email, password) }
            catch (e: FirebaseAuthException) { handleAuthError(e, onError) }
    }
}
