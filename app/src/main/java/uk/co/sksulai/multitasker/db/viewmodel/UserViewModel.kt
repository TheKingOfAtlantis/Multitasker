package uk.co.sksulai.multitasker.db.viewmodel

import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

import android.app.Application
import android.app.PendingIntent
import androidx.activity.result.*
import androidx.lifecycle.AndroidViewModel

import com.google.firebase.auth.FirebaseAuthException
import com.google.android.gms.auth.api.identity.*

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.ExperimentalCoroutinesApi

import uk.co.sksulai.multitasker.R
import uk.co.sksulai.multitasker.db.dao.QueryBuilder
import uk.co.sksulai.multitasker.db.model.UserModel
import uk.co.sksulai.multitasker.db.repo.GoogleIntent
import uk.co.sksulai.multitasker.db.repo.UserRepository

/**
 * Wrapper around ActivityResultLauncher to provide type overloading
 * @param launcher Activity result launcher to start the intent
 */
@JvmInline value class GoogleIntentLauncher(val launcher: ActivityResultLauncher<IntentSenderRequest>)
private fun GoogleIntentLauncher.launch(intent: PendingIntent) =
    launcher.launch(IntentSenderRequest.Builder(intent).build())
/**
 * Launches a BeginSignInResult intent
 */
private fun GoogleIntentLauncher.launch(intent: BeginSignInResult)  = launch(intent.pendingIntent)
/**
 * Launches a SavePasswordResult intent
 */
private fun GoogleIntentLauncher.launch(intent: SavePasswordResult) = launch(intent.pendingIntent)

/**
 * Viewmodel to access user data
 * @param app      The current application
 * @param userRepo The user repository to use
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel class UserViewModel @Inject constructor(
    private val app: Application,
    private val userRepo: UserRepository
) : AndroidViewModel(app) {
    /**
     * Flow of the current user
     */
    val currentUser = userRepo.currentUser
    /**
     * The current user's preferred home page
     */
    val preferredHome = currentUser.map { it?.PreferredHome }
    companion object {
        @VisibleForTesting
        val authErrorMessages = mapOf(
            "ERROR_INVALID_CUSTOM_TOKEN"                     to "The custom token format is incorrect. Please check the documentation.",
            "ERROR_CUSTOM_TOKEN_MISMATCH"                    to "The custom token corresponds to a different audience.",
            "ERROR_INVALID_CREDENTIAL"                       to "The supplied auth credential is malformed or has expired.",
            "ERROR_INVALID_EMAIL"                            to "The email address is badly formatted.",
            "ERROR_WRONG_PASSWORD"                           to "The password is invalid or the user does not have a password.",
            "ERROR_USER_MISMATCH"                            to "The supplied credentials do not correspond to the previously signed in user.",
            "ERROR_REQUIRES_RECENT_LOGIN"                    to "This operation is sensitive and requires recent authentication. Log in again before retrying this request.",
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" to "An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.",
            "ERROR_EMAIL_ALREADY_IN_USE"                     to "The email address is already in use by another account.",
            "ERROR_CREDENTIAL_ALREADY_IN_USE"                to "This credential is already associated with a different user account.",
            "ERROR_USER_DISABLED"                            to "The user account has been disabled by an administrator.",
            "ERROR_USER_TOKEN_EXPIRED"                       to "The user\'s credential is no longer valid. The user must sign in again.",
            "ERROR_USER_NOT_FOUND"                           to "There is no user record corresponding to this identifier. The user may have been deleted.",
            "ERROR_INVALID_USER_TOKEN"                       to "The user\'s credential is no longer valid. The user must sign in again.",
            "ERROR_OPERATION_NOT_ALLOWED"                    to "This operation is not allowed. You must enable this service in the console.",
            "ERROR_WEAK_PASSWORD"                            to "The password is invalid it must 6 characters at least",
        )
    }

    /**
     * Used to correctly show Firebase authentication errors to the user
     *
     * @param err             Firebase authentication exception to handle
     * @param onEmailError    Used to expose an error related to the given email
     * @param onPasswordError Used to expose an error related to the given password
     * @param onAuthError     Used to expose a more general authentication error
     */
    suspend fun handleAuthError(
        err: FirebaseAuthException,
        onEmailError: suspend (String) -> Unit,
        onPasswordError: suspend (String) -> Unit,
        onAuthError: suspend (String) -> Unit
    ) {
        // Copied from StackOverflow: https://stackoverflow.com/a/48503254/3856359
        val authError = authErrorMessages[err.errorCode] ?: throw err
        // Some error codes should be given as email or password errors instead
        when(err.errorCode) {
            "ERROR_INVALID_EMAIL",
            "ERROR_EMAIL_ALREADY_IN_USE" -> onEmailError(authError)
            "ERROR_WRONG_PASSWORD",
            "ERROR_WEAK_PASSWORD" -> onPasswordError(authError)
            else -> onAuthError(authError)
        }
    }

    /**
     * Common steps with regards to email sign-in/up
     * @param action        Handles unique sign-in/up details
     * @param email         The email to pass to the action handler
     * @param password      The password to pass to the action handler
     * @param saverLauncher Intent launcher to save the password
     */
    private suspend fun <T> emailAction(
        action: suspend (email: String, password: String) -> T,
        email: String,
        password: String,
        saverLauncher: GoogleIntentLauncher
    ) {
        action(email, password)
//      TODO: See if we can force the one-tap saver ui to appear
        val saver = Identity.getCredentialSavingClient(app)
            .savePassword(
                SavePasswordRequest.builder()
                    .setSignInPassword(SignInPassword(email, password))
                    .build()
            ).await()
        saverLauncher.launch(saver)
    }

    /**
     * Creates a user given an email and password
     * @param email         The email to use to create a user
     * @param password      The password to be used
     * @param saverLauncher Intent launcher used to save the email/password
     */
    suspend fun create(
        email: String,
        password: String,
        saverLauncher: GoogleIntentLauncher
    ) = emailAction(userRepo::create, email, password, saverLauncher)
    /**
     * Creates a user using the Google Identity API
     * @param launcher Intent launcher to open the One-tap sign in
     */
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

    /**
     * Authenticate a user given an email and password
     * @param email         The email to sign in with
     * @param password      The password to sign in with
     * @param saverLauncher Intent launcher to save the password
     */
    @Suppress("NAME_SHADOWING")
    suspend fun authenticate(
        email: String,
        password: String,
        saverLauncher: GoogleIntentLauncher
    ) = emailAction(
        { email, password -> userRepo.apply { authenticate(getCredentials(email, password)) } },
        email, password, saverLauncher
    )
    /**
     * Authenticate a user given the result of calling the Google Identity API
     * @param googleIntent Intent containing the result of signing in
     */
    suspend fun authenticate(googleIntent: GoogleIntent) =
        userRepo.apply { authenticate(getCredentials(googleIntent)) }
    /**
     * Authenticate a user using the Google Identity API
     * @param launcher Intent launcher to start the authentication
     */
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

    /**
     * Reauthenticate a user using their email & password
     * @param email The users email
     * @param password The users password
     */
    suspend fun reauthenticate(
        email: String,
        password: String
    ) = userRepo.apply { reauthenticate(getCredentials(email, password)) }

    suspend fun getProviders() = userRepo.getProviders()

    /**
     * Sign out the user
     */
    suspend fun signOut() {
        userRepo.signOut()
        Identity.getSignInClient(app).signOut().await()
    }

    /**
     * Update the users data
     * @param user The new updated user model
     */
    suspend fun update(user: UserModel) = userRepo.update(user)

    suspend fun updatePassword(oldPassword: String, newPassword: String) =
        userRepo.updatePassword(oldPassword, newPassword)

    /**
     * Deletes the current user from both the local and remote database
     */
    suspend fun delete() = currentUser.first()?.let {
        userRepo.delete(it, localOnly = false)
        signOut()
    }

    /**
     * Retrieves a UserModel given the user ID
     * @param id ID associated with the user to retrieve
     * @return Flow containing the UserModel (if found) or null
     */
    fun fromID(id: String) = userRepo.fromID(id)
    /**
     * Retrieves a list of UserModels given a search string to query against display names
     * @param displayName The display name search string
     * @param queryParams Builder to set the various query parameters
     * @return Flow containing the list of UserModels
     */
    fun fromDisplayName(displayName: String, queryParam: QueryBuilder.() -> Unit = {}) =
        userRepo.fromDisplayName(displayName, queryParam)
    /**
     * Retrieves a UserModel given the FirebaseUser object
     * @param user Firebase user to retrieve the associated UserModel
     * @return Flow containing the UserModel (if found) or null
     */
    fun fromActualName(displayName: String, queryParam: QueryBuilder.() -> Unit = {}) =
        userRepo.fromActualName(displayName, queryParam)

    suspend fun unlink(provider: UserRepository.AuthProvider) = userRepo.unlink(provider)

    /**
     * Provides methods for resetting passwords
     */
    val resetPassword get() = userRepo.resetPassword
    /**
     * Provides methods for verifying the email
     */
    val emailVerification get() = userRepo.emailVerification
}
