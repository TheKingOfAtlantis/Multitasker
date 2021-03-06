package uk.co.sksulai.multitasker.db.repo

import java.time.LocalDate

import android.net.Uri
import android.util.Log
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.edit

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import uk.co.sksulai.multitasker.util.asFlow

import com.bumptech.glide.Glide
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn

import com.google.firebase.auth.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth

import uk.co.sksulai.multitasker.db.LocalDB
import uk.co.sksulai.multitasker.db.dao.UserDao
import uk.co.sksulai.multitasker.db.model.UserModel
import uk.co.sksulai.multitasker.db.model.generateID
import uk.co.sksulai.multitasker.db.web.UserWebService
import uk.co.sksulai.multitasker.db.createDatabase

inline class GoogleIntent(val value: Intent?)

/**
 * The repository exposes all suspended functions as Flow<T>, this allows quick
 * integration with Jetpack Compose due to the collectAsState()
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserRepository(private val context: Context) {
    private val db: LocalDB         = LocalDB.createDatabase(context)
    private val dao: UserDao        = db.getUserDao()
    private val web: UserWebService = UserWebService()

    // Getters

    /**
     * @brief Reference to the current user which is signed in
     * The state of this value is automatically managed by the repository
     */
    val currentUser: StateFlow<UserModel?> get() = _currentUser
    private var _currentUser: MutableStateFlow<UserModel?> = MutableStateFlow(null)

    private suspend fun setCurrentUser(user: UserModel?) = withContext(Dispatchers.IO) {
        // Persist the change
        // Update the currentUser
        context.getSharedPreferences("auth", Context.MODE_PRIVATE).edit { putString("current_user", user?.ID) }
        _currentUser.value = user
    }

    init {
        MainScope().launch {
            // Load the persisted value
            // Update the currentUser
            val id = context.getSharedPreferences("auth", Context.MODE_PRIVATE).getString("current_user", null)
            if(id != null) _currentUser.value = dao.fromID(id)
        }
    }

    /**
     * Retrieves a UserModel given the user ID
     * @param id - The user ID to check for
     * @return Flow containing the UserModel (if found) or null
     */
    fun fromID(id: String): Flow<UserModel?> = flow { emit(
        // Check the local database first
        // If we haven't found the user try polling the internet
        dao.fromID(id) ?:
        web.fromID(id)
    )}
    /**
     * Retrieves a UserModel given the FirebaseUser object
     * @param id - The user to check for
     * @return Flow containing the UserModel (if found) or null
     */
    fun fromFirebase(user: FirebaseUser): Flow<UserModel?> = flow { emit(
        // Check the local database first
        // If we haven't found the user try polling the internet
        dao.fromFirebaseID(user.uid) ?:
        web.fromFirebase(user)
    )}
    /**
     * Retrieves a list of UserModels given a search string to query against display names
     * @param id - The display name search string
     * @return Flow containing the list of UserModels
     */
    fun fromDisplayName(displayName: String): Flow<List<UserModel>> =
        // Check the local database and the internet
        // Combine the resulting lists
        dao.fromDisplayName(displayName)
            .combine(web::fromDisplayName.asFlow(displayName)) { local, web -> local + web }
    /**
     * Retrieves a list of UserModels given a search string to query against names
     * @param id - The name search string
     * @return Flow containing the list of UserModels
     */
    fun fromActualName(actualName: String): Flow<List<UserModel>> =
        // Check the local database and the internet
        // Combine the resulting lists
        dao.fromActualName(actualName)
            .combine(web::fromActualName.asFlow(actualName)) { local, web -> local + web }

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
    private suspend fun create(user: FirebaseUser): UserModel = withContext(Dispatchers.IO) {
        create(
            firebaseId  = user.uid,
            email       = user.email,
            displayName = user.displayName,
            avatar      = user.photoUrl
        )
    }
    /**
     * Create a user given the values for various UserModel fields
     * @return The UserModel created for this user
     */
    suspend fun create(
        id: String            = generateID(),
        firebaseId: String    = "",
        displayName: String?  = null,
        email: String?        = null,
        avatar: Uri?          = null,
        actualName: String?   = null,
        homeLocation: String? = null,
        dob: LocalDate?       = null
    ): UserModel = withContext(Dispatchers.IO) {
        create(UserModel(
            ID          = id,
            FirebaseID  = firebaseId,
            DisplayName = displayName,
            Email       = email,
            Avatar      = avatar,
            ActualName  = actualName,
            Home        = homeLocation,
            DOB         = dob
        ))
    }
    /**
     * Create a user given a UserModel object
     * @return The UserModel passed for this user
     */
    private suspend fun create(model: UserModel): UserModel = withContext(Dispatchers.IO) {
        model.also {
            insert(model)
            setCurrentUser(model)
        }
    }

    /**
     * Insert the UserModel value into the local database + Firestore
     */
    suspend fun insert(user: UserModel): Unit = withContext(Dispatchers.IO) {
        launch { dao.insert(user) }
        launch { web.insert(user) }
        return@withContext
    }

    // Update
    /**
     * Updates the users information
     * @param model - UserModel with the modifications to make
     */
    suspend fun update(user: UserModel): Unit = withContext(Dispatchers.IO) {
        launch { dao.update(user) }
        launch { web.update(user) }
        return@withContext
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
            user?.let { dao.delete(it) }
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
    suspend fun authenticate(email: String, password: String): String =
        authenticate(EmailAuthProvider.getCredential(email, password))
    /**
     * Authenticate the user using the Google Sign In APIs
     */
    suspend fun authenticate(googleIntent: GoogleIntent): String = withContext(Dispatchers.IO) {
        val googleUser = GoogleSignIn.getSignedInAccountFromIntent(googleIntent.value).await()
        authenticate(GoogleAuthProvider.getCredential(googleUser.idToken, null))
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

    private suspend fun authenticate(credential: AuthCredential): String {
        // Authenticate the user w/ credential using Firebase
        // Once we succeed retrieve user information from database
        // Insert this information into the local database
        // Set the current user value
        // return the user ID

        val authResult = Firebase.auth.signInWithCredential(credential).await()
        return authResult.user!!.let {
            val user = web.fromFirebase(it)!!
            dao.insert(user)
            setCurrentUser(user)
            user.ID
        }
    }

    /**
     * Link the user account to the Google Provider
     */
    suspend fun link(googleIntent: GoogleIntent) = withContext(Dispatchers.IO) {
        val googleAccount = GoogleSignIn.getSignedInAccountFromIntent(googleIntent.value).await()
        link(GoogleAuthProvider.getCredential(googleAccount.idToken, null))
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
        setCurrentUser(null)
        Firebase.auth.signOut()
    }

    suspend fun getAvatar(user: UserModel): Bitmap? = withContext(Dispatchers.IO) {
        if(user.Avatar == null) null
        else Glide.with(context)
                  .asBitmap()
                  .load(user.Avatar)
                  .submit()
                  .get()
    }

    suspend fun verifyEmail() = withContext(Dispatchers.IO) {
        Firebase.auth.currentUser?.sendEmailVerification()?.await()
    }
    fun isEmailVerified()= Firebase.auth.currentUser?.isEmailVerified ?: false
}
