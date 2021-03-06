package uk.co.sksulai.multitasker.db.viewmodel

import android.app.Application
import android.app.PendingIntent
import androidx.activity.result.*
import androidx.lifecycle.AndroidViewModel

import com.google.firebase.auth.FirebaseAuthException
import com.google.android.gms.auth.api.identity.*
import com.facebook.login.LoginResult

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

import uk.co.sksulai.multitasker.R
import uk.co.sksulai.multitasker.db.repo.GoogleIntent
import uk.co.sksulai.multitasker.db.repo.UserRepository

inline class GoogleIntentLauncher(val value: ActivityResultLauncher<IntentSenderRequest>)
private fun GoogleIntentLauncher.launch(intent: PendingIntent) =
    value.launch(IntentSenderRequest.Builder(intent).build())
private fun GoogleIntentLauncher.launch(intent: BeginSignInResult)  = launch(intent.pendingIntent)
private fun GoogleIntentLauncher.launch(intent: SavePasswordResult) = launch(intent.pendingIntent)

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModel(private val app: Application) : AndroidViewModel(app) {
    private val userRepo by lazy { UserRepository(app) }

    val currentUser   = userRepo.currentUser
    val preferredHome = currentUser.map { it?.PreferredHome }

    suspend fun handleAuthError(
        err: FirebaseAuthException,
        onEmailError: suspend (String) -> Unit,
        onPasswordError: suspend (String) -> Unit,
        onAuthError: suspend (String) -> Unit
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
            "ERROR_EMAIL_ALREADY_IN_USE" -> onEmailError(authError)
            "ERROR_WRONG_PASSWORD",
            "ERROR_WEAK_PASSWORD" -> onPasswordError(authError)
            else -> onAuthError(authError)
        }
    }

    private suspend fun <T> action(
        action: suspend (email: String, password: String) -> T,
        email: String,
        password: String,
        saverLauncher: GoogleIntentLauncher
    ) {
        action(email, password)
//        TODO: Once we can force the one-tap saver ui to appear switch to it
//        val saver = Identity.getCredentialSavingClient(app)
//            .savePassword(
//                SavePasswordRequest.builder()
//                    .setSignInPassword(SignInPassword(email, password))
//                    .build()
//            ).await()
//        saverLauncher.launch(saver)
    }

    suspend fun create(
        email: String,
        password: String,
        saverLauncher: GoogleIntentLauncher
    ) = action(userRepo::create, email, password, saverLauncher)
    suspend fun create(launcher: GoogleIntentLauncher) {
        val request = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setServerClientId(app.getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .setSupported(true)
                    .build()
            ).build()
        val intent = Identity.getSignInClient(app)
            .beginSignIn(request)
            .await()
        launcher.launch(intent)
    }

    suspend fun authenticate(
        email: String,
        password: String,
        saverLauncher: GoogleIntentLauncher
    ) = action(userRepo::authenticate, email, password, saverLauncher)
    suspend fun authenticate(loginResult: LoginResult) { userRepo.authenticate(loginResult.accessToken) }
    suspend fun authenticate(googleIntent: GoogleIntent) { userRepo.authenticate(googleIntent) }
    suspend fun authenticate(launcher: GoogleIntentLauncher) {
        val request = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setServerClientId(app.getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(true)
                    .setSupported(true)
                    .build()
            ).setPasswordRequestOptions(
                BeginSignInRequest.PasswordRequestOptions.builder()
                    .setSupported(true)
                    .build()
            ).build()
        val intent = Identity.getSignInClient(app)
            .beginSignIn(request)
            .await()
        launcher.launch(intent)
    }

    suspend fun signOut() {
        userRepo.signOut()
        Identity.getSignInClient(app).signOut().await()
    }

    val resetPassword     get() = userRepo.resetPassword
    val emailVerification get() = userRepo.emailVerification
}
