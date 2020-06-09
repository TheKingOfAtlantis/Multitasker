package uk.co.sksulai.multitasker.db.repo

import java.time.LocalDate

import android.net.Uri
import android.content.Context

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignIn

import com.google.firebase.auth.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth

import uk.co.sksulai.multitasker.db.LocalDB
import uk.co.sksulai.multitasker.db.dao.UserDao
import uk.co.sksulai.multitasker.db.model.UserModel
import uk.co.sksulai.multitasker.db.model.generateID
import uk.co.sksulai.multitasker.db.createDatabase

inline class GoogleIntent(val value: Intent?)
@OptIn(ExperimentalCoroutinesApi::class)
class UserRepository(private val context: Context) {
    private val db: LocalDB  = LocalDB.createDatabase(context)
    private val dao: UserDao = db.getUserDao()

    /**
     * @brief Reference to the current user which is signed in
     * The state of this value is automatically managed by the repository
     */
    val currentUser: StateFlow<UserModel?>
        get() = _currentUser
    private var _currentUser: MutableStateFlow<UserModel?> = MutableStateFlow(null)

    private suspend fun setCurrentUser(user: UserModel?) = withContext(Dispatchers.IO) {
        context.getSharedPreferences("auth", Context.MODE_PRIVATE).edit { putString("current_user", user?.ID) }
        _currentUser.value = user
    }

    init {
        MainScope().launch {
            val id = context.getSharedPreferences("auth", Context.MODE_PRIVATE).getString("current_user", null)
            if(id != null) _currentUser.value = dao.fromID(id)
        }
    }

    /**
     *
     */
    suspend fun fromID(id: String): UserModel? = withContext(Dispatchers.IO) {
        dao.fromID(id)
    }
    suspend fun fromFirebase(user: FirebaseUser): UserModel? = withContext(Dispatchers.IO) {
        dao.fromFirebaseID(user.uid)
    }
    fun fromDisplayName(name: String): Flow<List<UserModel>> {
        return dao.fromDisplayName(name)
    }
    fun fromActualName(name: String): Flow<List<UserModel>> {
        return dao.fromActualName(name)
    }

    // Creation: Used when user creates an account

    suspend fun create(
        email: String,
        password: String
    ) = withContext(Dispatchers.IO) {
        val authResult = Firebase.auth.createUserWithEmailAndPassword(email, password).await()
        create(authResult.user!!)
    }
    suspend fun create(user: FirebaseUser): UserModel = withContext(Dispatchers.IO) {
        create(
            firebaseId  = user.uid,
            email       = user.email,
            displayName = user.displayName,
            avatar      = user.photoUrl
        )
    }
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
            ID = id,
            FirebaseID = firebaseId,
            DisplayName = displayName,
            Email = email,
            Avatar = avatar,
            ActualName = actualName,
            Home = homeLocation,
            DOB = dob
        ))
    }
    suspend fun create(model: UserModel): UserModel = withContext(Dispatchers.IO) {
        model.also {
            insert(model)
            setCurrentUser(model)
        }
    }

    // Update

    /**
     * Updates the users information
     * @param model - UserModel with the modifications to make
     */
    suspend fun update(model: UserModel) = withContext(Dispatchers.IO) {
        dao.update(model)
    }

    // Delete

    /**
     * Deletes the user given the UserModel
     * @param id ID of the user to be deleted (UserModel#ID)
     * @param localOnly Whether to just delete from local database or propagate to Firebase
     */
    suspend fun delete(model: UserModel, localOnly: Boolean = true) = withContext(Dispatchers.IO) {
        dao.delete(model)
        if(!localOnly)
            TODO("Need to implement deleting firebase users")
    }
    /**
     * Deletes the user given an ID
     * @param id ID of the user to be deleted (UserModel#ID)
     * @param localOnly Whether to just delete from local database or propagate to Firebase
     */
    suspend fun delete(id: String, localOnly: Boolean = true) = withContext(Dispatchers.IO) {
        fromID(id)?.let { delete(it, localOnly) }
    }
    /**
     * Deletes the user given the Firebase user object
     * @param id ID of the user to be deleted (UserModel#ID)
     * @param localOnly Whether to just delete from local database or propagate to Firebase
     */
    suspend fun delete(user: FirebaseUser, localOnly: Boolean = true) = withContext(Dispatchers.IO) {
        fromFirebase(user)?.let { delete(it, localOnly) }
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
    private suspend fun authenticate(credential: AuthCredential): String {
        // Authenticate the user w/ credential using Firebase
        // Once we succeed retrieve user information from database
        // Insert this information into the local database
        // Set the current user value
        // return the user ID

        val authResult = Firebase.auth.signInWithCredential(credential).await()
        return authResult.user!!.let {
            // TODO: Replace w/ data from Firebase
            val user = create(it)
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

    private suspend fun link(credential: AuthCredential) {
        Firebase.auth.currentUser?.linkWithCredential(credential)?.await()
    }
    private suspend fun unlink(provider: String) {
        Firebase.auth.currentUser?.unlink(provider)?.await()
    }

    /**
     * Signs the current user out
     */
    fun signOut() {
        setCurrentUser(null)
        Firebase.auth.signOut()
    }
}
