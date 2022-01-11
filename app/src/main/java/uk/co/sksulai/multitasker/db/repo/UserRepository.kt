package uk.co.sksulai.multitasker.db.repo

import java.time.Instant
import java.time.LocalDate

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

import android.net.Uri
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.edit

import com.google.firebase.auth.*
import com.google.android.gms.auth.api.identity.Identity

import uk.co.sksulai.multitasker.db.dao.*
import uk.co.sksulai.multitasker.db.web.*
import uk.co.sksulai.multitasker.db.model.*
import uk.co.sksulai.multitasker.di.DispatcherIO
import uk.co.sksulai.multitasker.util.DatastoreLocators.AppState

@JvmInline value class GoogleIntent(val value: Intent?)

//
// The repository exposes all suspended functions as Flow<T>, this allows quick
// integration with Jetpack Compose due to the collectAsState()
// Also integrates data from both local and remote data
//

/**
 * Used to manage access to user data both locally and remotely
 * @param context Application context
 * @param dao Local user database access object
 * @param web Firebase user database
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: UserDao,
    private val web: UserWebService,
    private val firebaseAuth: FirebaseAuth,
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher
) {
    // Getters

    /**
     * Reference to the current user which is signed in
     * The state of this value is automatically managed by the repository
     */
    val currentUser = AppState.retrieve(context).data
        .map { it[AppState.CurrentUser] }
        .transformLatest { id -> id?.let { emitAll(fromID(it)) } ?: emit(null) }

    /**
     * Used when authenticating to set the current user
     * @param id ID of the new current user
     */
    private suspend fun setCurrentUser(id: String?) = AppState.retrieve(context).edit {
        if(id.isNullOrEmpty())
            it.remove(AppState.CurrentUser)
        else it[AppState.CurrentUser] = id
    }

    /**
     * Retrieves all the users stored locally
     * @return Flow of users
     */
    fun getAll() = dao.getAll().flowOn(ioDispatcher)

    /**
     * Retrieves a UserModel given the user ID
     * @param id ID associated with the user to retrieve
     * @return Flow containing the UserModel (if found) or null
     */
    fun fromID(id: String) = web.fromID(id)
        .onEach { user -> user?.let { dao.insert(it) } }
        .catch { emitAll(dao.fromID(id)) }
        .flowOn(ioDispatcher)

    /**
     * Retrieves a UserModel given the FirebaseUser object
     * @param user Firebase user to retrieve the associated UserModel
     * @return Flow containing the UserModel (if found) or null
     */
    fun fromFirebase(user: FirebaseUser) = fromID(user.uid)
    /**
     * Retrieves a list of UserModels given a search string to query against display names
     * @param displayName The display name search string
     * @param queryParams Builder to set the various query parameters
     * @return Flow containing the list of UserModels
     */
    fun fromDisplayName(displayName: String, queryParams: QueryBuilder.() -> Unit = {}) = combine(
        // Check the local database and the internet
        // Combine the resulting lists
        dao.fromDisplayName(SearchQuery.local(displayName, queryParams)),
        web.fromDisplayName(SearchQuery.remote(displayName, queryParams))
    ) { local, web -> (local + web).distinctBy { it.ID }  }.flowOn(ioDispatcher)
    /**
     * Retrieves a list of UserModels given a search string to query against names
     * @param actualName  The name search string
     * @param queryParams Builder to set the various query parameters
     * @return Flow containing the list of UserModels
     */
    fun fromActualName(actualName: String, queryParams: QueryBuilder.() -> Unit = {}) = combine(
        // Check the local database and the internet
        // Combine the resulting lists
        dao.fromActualName(SearchQuery.local(actualName, queryParams)),
        web.fromActualName(SearchQuery.remote(actualName, queryParams))
    ) { local, web -> (local + web).distinctBy { it.ID } }.flowOn(ioDispatcher)

    // Creation: Used when user creates an account

    /**
     * Create a user given a email and password
     * @param email    The email to associate the account with
     * @param password The password to use for the account
     * @return The UserModel created for this user
     */
    suspend fun create(
        email: String,
        password: String
    ) = withContext(ioDispatcher) {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        create(authResult.user!!)
    }

    /**
     * Create a user given a Firebase User object
     * @param user FirebaseUser object to create an account for
     * @return The UserModel created for this user
     */
    private suspend fun create(user: FirebaseUser) = create(
        id          = user.uid,
        email       = user.email,
        displayName = user.displayName,
        avatar      = user.photoUrl
    )

    /**
     * Create a user given the values for various UserModel fields
     * @return The UserModel created for this user
     */
    private suspend fun create(
        id: String,
        displayName: String?  = null,
        email: String?        = null,
        preferredHome: String = "Dashboard",
        avatar: Uri?          = null,
        actualName: String?   = null,
        homeLocation: String? = null,
        dob: LocalDate?       = null
    ) = create(UserModel(
        ID            = id,
        Creation      = Instant.now(),
        LastModified  = Instant.now(),
        Email         = email,
        DisplayName   = displayName,
        PreferredHome = preferredHome,
        Avatar        = avatar,
        ActualName    = actualName,
        Home          = homeLocation,
        DOB           = dob
    ))

    /**
     * Create a user given a UserModel object
     * @return The UserModel passed for this user
     */
    private suspend fun create(user: UserModel) = user.also {
        setCurrentUser(user.ID)
        insert(user)
    }

    /**
     * Insert the UserModel value into the local database + Firestore
     */
    suspend fun insert(user: UserModel): Unit = withContext(ioDispatcher) {
        dao.insert(user)
        web.insert(user)
    }

    // Update
    /**
     * Updates the users information
     * @param model UserModel with the modifications to make
     */
    suspend fun update(user: UserModel): Unit = withContext(ioDispatcher) {
        user.copy(
            LastModified = Instant.now(),
            Avatar       = updateAvatar(user)
        ).also { user ->
            dao.update(user)
            web.update(user)
        }
    }

    /**
     *
     * @param user User with the updated avatar
     * @return The Uri for the avatar
     */
    suspend fun updateAvatar(user: UserModel) =
        if(currentUser.first()?.Avatar != user.Avatar) {
            if(user.Avatar != null) web.uploadAvatar(user.ID, user.Avatar)
            else web.deleteAvatar(user.ID).let { null }
        } else user.Avatar
    // Delete
    /**
     * Deletes the user given the UserModel user object
     * @param user user to be deleted (UserModel#ID)
     * @param localOnly Whether to just delete from local database or propagate to Firebase
     */
    suspend fun delete(user: UserModel, localOnly: Boolean = true): Unit = withContext(ioDispatcher) {
        val current = currentUser.first()

        dao.delete(user)
        if(!localOnly) {
            web.delete(user)
            setCurrentUser(null)
        } else if(current == user) signOut()
    }
    /**
     * Deletes the user given the user ID
     * @param id ID of the user to be deleted (UserModel#ID)
     * @param localOnly Whether to just delete from local database or propagate to Firebase
     */
    suspend fun delete(id: String, localOnly: Boolean = true) =
        fromID(id).first()?.let { delete(it, localOnly) }
    /**
     * Deletes the user given the Firebase user object
     * @param id ID of the user to be deleted (UserModel#ID)
     * @param localOnly Whether to just delete from local database or propagate to Firebase
     */
    suspend fun delete(user: FirebaseUser, localOnly: Boolean = true) =
        fromFirebase(user).first()?.let { delete(it, localOnly) }

    // User authentication & account linking

    /**
     * Authenticate the user using email & password against the credentials stored by firebase
     * @param email Email to authenticate against
     * @param password Password to check
     */
    fun getCredentials(email: String, password: String) =
        EmailAuthProvider.getCredential(email, password)
    /**
     * Authenticate the user using the Google Sign In APIs
     */
    suspend fun getCredentials(googleIntent: GoogleIntent) = withContext(ioDispatcher) {
        val googleUser = Identity.getSignInClient(context)
            .getSignInCredentialFromIntent(googleIntent.value)

        val idToken  = googleUser.googleIdToken
        val email    = googleUser.id
        val password = googleUser.password

        when {
            !idToken.isNullOrEmpty()  -> GoogleAuthProvider.getCredential(idToken, null)
            !password.isNullOrEmpty() -> EmailAuthProvider.getCredential(email, password)
            else -> throw Exception("We received neither a Id Token or Email/Password")
        }
    }

    /**
     * Used to perform authentication via firebase
     * @param credential The credentials which have been retrieves by a credential provider
     */
    suspend fun authenticate(credential: AuthCredential): String {
        // Authenticate the user w/ credential using Firebase
        // Once we succeed retrieve user information from database
        // Insert this information into the local database
        // Set the current user value
        // return the user ID

        val authResult = firebaseAuth.signInWithCredential(credential).await()
        authResult.user!!.let {
            web.fromFirebase(it).first()?.let { user -> dao.insert(user) }
            setCurrentUser(it.uid)
            it.uid
        }
    }

    /**
     * Reauthenticates a user - To be user prior to performing sensitive operations
     * @param credential The credentials to be used for reauthentication
     */
    suspend fun reauthenticate(credential: AuthCredential) {
        firebaseAuth.currentUser?.reauthenticate(credential)?.await()
    }
    /**
     * Link the user account to the Google Provider
     */
    suspend fun link(googleIntent: GoogleIntent) = withContext(ioDispatcher) {
        val googleAccount = Identity.getSignInClient(context).getSignInCredentialFromIntent(googleIntent.value)
        GoogleAuthProvider.getCredential(googleAccount.googleIdToken, null)
    }

    /**
     * Link the user account to the Facebook Provider
     */
    suspend fun link(credential: AuthCredential) {
        firebaseAuth.currentUser
            ?.linkWithCredential(credential)
            ?.await()
    }
    /**
     * Dissociate the user account from a login provider
     */
    suspend fun unlink(provider: String) {
        firebaseAuth.currentUser
            ?.unlink(provider)
            ?.await()
    }

    /**
     * Signs the current user out
     */
    suspend fun signOut() {
        setCurrentUser(null)
        firebaseAuth.signOut()
    }

    // Email & Password Actions

    /**
     * Provides methods for resetting passwords
     */
    inner class ResetPassword {
        /**
         * Sends a request to reset a password to the given email
         * @param email THe email to send the request to/associated with the account
         */
        suspend fun request(email: String): Unit = withContext(ioDispatcher) {
            firebaseAuth.sendPasswordResetEmail(email).await()
        }
        /**
         * Checks that the code from the request is valid
         * @param code Password reset request code from the deeplink
         */
        suspend fun isValid(code: String): String = withContext(ioDispatcher) {
            firebaseAuth.verifyPasswordResetCode(code).await()
        }
        /**
         * Performs the resetting of the password
         *
         * @param code Password reset request code
         * @param email The email associated with the user
         * @param password The new password to use for the user
         */
        suspend fun reset(code: String, email: String, password: String): Unit = withContext(ioDispatcher) {
            firebaseAuth.confirmPasswordReset(code, password).await()
            authenticate(getCredentials(email, password))
        }
    }

    /**
     * Provides methods for verifying the email
     */
    inner class EmailVerification {
        /**
         * Whether or not the email of the current user has been verified
         */
        val verified: Boolean = firebaseAuth.currentUser?.isEmailVerified ?: false
        /**
         * Sends an email verification request to the current user's email
         */
        suspend fun request() = withContext(ioDispatcher) { firebaseAuth.currentUser?.sendEmailVerification()?.await() }
        /**
         * Verifies the email verification code retrieved from deeplink and if valid
         * marks the user as verified
         *
         * @param code The email verification code to verify
         */
        suspend fun confirm(code: String) = withContext(ioDispatcher) { firebaseAuth.applyActionCode(code).await() }
    }

    /**
     * Provides methods for resetting passwords
     */
    val resetPassword = ResetPassword()
    /**
     * Provides methods for verifying the email
     */
    val emailVerification = EmailVerification()
}
