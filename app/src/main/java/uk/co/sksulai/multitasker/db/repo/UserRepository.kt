package uk.co.sksulai.multitasker.db.repo

import java.time.Instant
import java.time.LocalDate

import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

import android.net.Uri
import android.util.Log
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.edit

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

import com.facebook.AccessToken
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.identity.Identity

import com.google.firebase.auth.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth

import uk.co.sksulai.multitasker.db.dao.UserDao
import uk.co.sksulai.multitasker.db.model.UserModel
import uk.co.sksulai.multitasker.db.web.UserWebService
import uk.co.sksulai.multitasker.util.DatastoreLocators.AppState

@JvmInline value class GoogleIntent(val value: Intent?)

/**
 * The repository exposes all suspended functions as Flow<T>, this allows quick
 * integration with Jetpack Compose due to the collectAsState()
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: UserDao,
    private val web: UserWebService
) {
    private val repoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Getters

    /**
     * Reference to the current user which is signed in
     * The state of this value is automatically managed by the repository
     */
    val currentUser = AppState.retrieve(context).data
        .map { it[AppState.CurrentUser] ?: "" }
        .flatMapLatest { fromID(it) }
        .flowOn(Dispatchers.IO)

    private suspend fun setCurrentUser(value: String) = repoScope.launch {
        AppState.retrieve(context).edit { it[AppState.CurrentUser] = value }
    }

    fun getAll() = dao.getAll()

    /**
     * Retrieves a UserModel given the user ID
     * @param id ID associated with the user to retrieve
     * @return Flow containing the UserModel (if found) or null
     */
    fun fromID(id: String) = combine(
        // Check the local database first
        // If we haven't found the user try polling the internet
        dao.fromID(id),
        web.fromID(id)
    ) { db, web -> db ?: web }.flowOn(Dispatchers.IO)

    /**
     * Retrieves a UserModel given the FirebaseUser object
     * @param user Firebase user to retrieve the associated UserModel
     * @return Flow containing the UserModel (if found) or null
     */
    fun fromFirebase(user: FirebaseUser) = fromID(user.uid)
    /**
     * Retrieves a list of UserModels given a search string to query against display names
     * @param id - The display name search string
     * @return Flow containing the list of UserModels
     */
    fun fromDisplayName(displayName: String) = combine(
        // Check the local database and the internet
        // Combine the resulting lists
        dao.fromDisplayName(displayName),
        web.fromDisplayName(displayName)
    ) { local, web -> local + web }.flowOn(Dispatchers.IO)
    /**
     * Retrieves a list of UserModels given a search string to query against names
     * @param id - The name search string
     * @return Flow containing the list of UserModels
     */
    fun fromActualName(actualName: String) = combine(
        // Check the local database and the internet
        // Combine the resulting lists
        dao.fromActualName(actualName),
        web.fromActualName(actualName)
    ) { local, web -> local + web }.flowOn(Dispatchers.IO)

    // Creation: Used when user creates an account

    /**
     * Create a user given a email and password
     * @param email - The email to associate the account with
     * @param password - The password to use for the account
     * @return The UserModel created for this user
     */
    suspend fun create(
        email: String,
        password: String
    ) = withContext(Dispatchers.IO) {
        val authResult = Firebase.auth.createUserWithEmailAndPassword(email, password).await()
        create(authResult.user!!)
    }

    /**
     * Create a user given a Firebase User object
     * @param user - FirebaseUser object to create an account for
     * @return The UserModel created for this user
     */
    private suspend fun create(user: FirebaseUser) = withContext(Dispatchers.IO) {
        create(
            id          = user.uid,
            email       = user.email,
            displayName = user.displayName,
            avatar      = user.photoUrl
        )
    }
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
    ): UserModel = withContext(Dispatchers.IO) {
        create(UserModel(
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
    }
    /**
     * Create a user given a UserModel object
     * @return The UserModel passed for this user
     */
    private suspend fun create(model: UserModel) = withContext(Dispatchers.IO) {
        model.also {
            insert(it)
            setCurrentUser(it.ID)
        }
    }

    /**
     * Insert the UserModel value into the local database + Firestore
     */
    suspend fun insert(user: UserModel): Unit = withContext(Dispatchers.IO) {
        launch { dao.insert(user) }
        launch { web.insert(user) }
    }

    // Update
    /**
     * Updates the users information
     * @param model - UserModel with the modifications to make
     */
    suspend fun update(user: UserModel): Unit = withContext(Dispatchers.IO) {
        launch { dao.update(user.copy(LastModified = Instant.now())) }
        launch { web.update(user.copy(LastModified = Instant.now())) }
    }

    // Delete
    /**
     * Deletes the user given the UserModel user object
     * @param user user to be deleted (UserModel#ID)
     * @param localOnly Whether to just delete from local database or propagate to Firebase
     */
    suspend fun delete(user: UserModel, localOnly: Boolean = true) = withContext(Dispatchers.IO) {
        launch { dao.delete(user) }
        if(!localOnly) launch {
            // TODO: Should only be done by owner of account
            //       Do we need to check this here?
            web.delete(user)
        }
    }
    /**
     * Deletes the user given the user ID
     * @param id ID of the user to be deleted (UserModel#ID)
     * @param localOnly Whether to just delete from local database or propagate to Firebase
     */
    suspend fun delete(id: String, localOnly: Boolean = true) = withContext(Dispatchers.IO) {
        launch {
            val user = dao.fromID(id)
            user.single()?.let { dao.delete(it) }
        }
        if(!localOnly) launch { web.delete(id) }
    }
    /**
     * Deletes the user given the Firebase user object
     * @param id ID of the user to be deleted (UserModel#ID)
     * @param localOnly Whether to just delete from local database or propagate to Firebase
     */
    suspend fun delete(user: FirebaseUser, localOnly: Boolean = true) = withContext(Dispatchers.IO) {
        fromFirebase(user).collectLatest { user ->
            user?.let { delete(it, localOnly) }
        }
    }

    // User authentication & account linking

    /**
     * Authenticate the user against the credentials stored by firebase
     * @param email Email to authenticate against
     * @param password Password to check
     */
    suspend fun authenticate(email: String, password: String) =
        authenticate(EmailAuthProvider.getCredential(email, password))

    /**
     * Authenticate the user using the Google Sign In APIs
     */
    suspend fun authenticate(googleIntent: GoogleIntent) = withContext(Dispatchers.IO) {
        val googleUser = Identity.getSignInClient(context)
            .getSignInCredentialFromIntent(googleIntent.value)

        val idToken  = googleUser.googleIdToken
        val email    = googleUser.id
        val password = googleUser.password

        authenticate(when {
            idToken  != null -> GoogleAuthProvider.getCredential(idToken, null)
            password != null -> EmailAuthProvider.getCredential(email, password)
            else -> throw Exception("We received neither a Id Token or Email/Password")
        })
    }
    /**
     * Authenticate the user using the Facebook APIs
     */
    suspend fun authenticate(facebook: AccessToken) = withContext(Dispatchers.IO) {
        authenticate(FacebookAuthProvider.getCredential(facebook.token))
    }
    fun getFacebookCallback() = object : FacebookCallback<LoginResult> {
        override fun onSuccess(result: LoginResult) {
            Log.d("auth", "facebook:onSuccess:$result")
            MainScope().launch { authenticate(result.accessToken) }
        }

        override fun onCancel() {
            Log.d("auth", "facebook:onCancel")
            TODO("Not yet implemented")
        }

        override fun onError(error: FacebookException?) {
            Log.d("auth", "facebook:onError:", error)
            TODO("Not yet implemented")
        }
    }

    /**
     * Used to perform authentication via firebase
     * @param credential The credentials which have been retrieves by a credential provider
     */
    private suspend fun authenticate(credential: AuthCredential) = withContext(Dispatchers.IO) {
        // Authenticate the user w/ credential using Firebase
        // Once we succeed retrieve user information from database
        // Insert this information into the local database
        // Set the current user value
        // return the user ID

        val authResult = Firebase.auth.signInWithCredential(credential).await()
        authResult.user!!.let {

            // Fixed: Appears that this flow was never returning a value
            // So using StateFlow to ensure that it is hot and thus has a value
            web.fromFirebase(it).first()?.also { user ->
                dao.insert(user)
                setCurrentUser(user.ID)
            }?.ID ?: ""
        }
    }

    /**
     * Link the user account to the Google Provider
     */
    suspend fun link(googleIntent: GoogleIntent) = withContext(Dispatchers.IO) {
        val googleAccount = Identity.getSignInClient(context).getSignInCredentialFromIntent(googleIntent.value)
        link(GoogleAuthProvider.getCredential(googleAccount.googleIdToken, null))
    }
    /**
     * Link the user account to the Facebook Provider
     */
    suspend fun link(facebook: AccessToken) = withContext(Dispatchers.IO) {
        link(FacebookAuthProvider.getCredential(facebook.token))
    }

    /**
     * Link the user account to the Facebook Provider
     */
    private suspend fun link(credential: AuthCredential) {
        Firebase.auth.currentUser?.linkWithCredential(credential)?.await()
    }
    /**
     * Dissociate the user account from a login provider
     */
    private suspend fun unlink(provider: String) {
        Firebase.auth.currentUser?.unlink(provider)?.await()
    }

    /**
     * Signs the current user out
     */
    suspend fun signOut() {
        setCurrentUser("")
        Firebase.auth.signOut()
    }

    // Email & Password Actions

    interface ResetPassword {
        suspend fun request(email: String)
        suspend fun isValid(code: String): String
        suspend fun reset(code: String, email: String, password: String)
    }
    interface EmailVerification {
        val verified: Boolean
        suspend fun request()
        suspend fun confirm(code: String)
    }

    /**
     * Provides methods for resetting passwords
     */
    val resetPassword = object : ResetPassword {
        /**
         * Sends a request to reset a password to the given email
         * @param email THe email to send the request to/associated with the account
         */
        override suspend fun request(email: String): Unit = withContext(Dispatchers.IO) {
            Firebase.auth.sendPasswordResetEmail(email).await()
        }

        /**
         * Checks that the code from the request is valid
         * @param code Password reset request code from the deeplink
         */
        override suspend fun isValid(code: String) = withContext(Dispatchers.IO) {
            Firebase.auth.verifyPasswordResetCode(code).await()
        }

        /**
         * Performs the resetting of the password
         *
         * @param code Password reset request code
         * @param email The email associated with the user
         * @param password The new password to use for the user
         */
        override suspend fun reset(code: String, email: String, password: String): Unit = withContext(Dispatchers.IO) {
            Firebase.auth.confirmPasswordReset(code, password).await()
            authenticate(email, password)
        }
    }
    /**
     * Provides methods for verifying the email
     */
    val emailVerification = object : EmailVerification {
        /**
         * Whether or not the email of the current user has been verified
         */
        override val verified = Firebase.auth.currentUser?.isEmailVerified ?: false

        /**
         * Sends an email verification request to the current user's email
         */
        override suspend fun request() {
            Firebase.auth.currentUser?.sendEmailVerification()
        }
        /**
         * Verifies the email verification code retrieved from deeplink and if valid
         * marks the user as verified
         *
         * @param code The email verification code to verify
         */
        override suspend fun confirm(code: String) {
            Firebase.auth.applyActionCode(code).await()
        }
    }
}
