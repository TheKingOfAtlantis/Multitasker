package uk.co.sksulai.multitasker.db.repo

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.google.firebase.auth.*
import kotlinx.coroutines.flow.Flow

import uk.co.sksulai.multitasker.db.LocalDB
import uk.co.sksulai.multitasker.db.dao.UserDao
import uk.co.sksulai.multitasker.db.model.UserModel
import uk.co.sksulai.multitasker.db.model.generateID
import java.time.LocalDate

class UserRepository(
    val db: LocalDB,
    val dao: UserDao
) {

    /**
     * @brief Reference to the current user which is signed in
     * The state of this value is automatically managed by the repository
     */
    val currentUser: UserModel?
        get() = currentUserState
    private var currentUserState: UserModel? = null // currentUser backing property

    // Getters

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

    suspend fun create(user: FirebaseUser): UserModel = withContext(Dispatchers.IO) {
        create(
            firebaseId   = user.uid,
            displayName  = user.displayName,
            email        = user.email,
            avatar       = user.photoUrl,
            actualName   = null,
            homeLocation = null,
            dob          = null
        )
    }
    suspend fun create(
        id: String = generateID(),
        firebaseId: String = "",
        displayName: String? = null,
        email: String? = null,
        avatar: Uri? = null,
        actualName: String? = null,
        homeLocation: String? = null,
        dob: LocalDate? = null
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
        dao.insert(model)
        return@withContext model
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
    suspend fun authenticateWithEmail(
        email: String,
        password: String
    ) = withContext(Dispatchers.IO) { TODO() }
    /**
     * Authenticate the user using the Google Sign In APIs
     */
    suspend fun authenticateWithGoogle() = withContext(Dispatchers.IO) { TODO() }
    /**
     * Authenticate the user using the Facebook APIs
     */
    suspend fun authenticateWithFacebook() = withContext(Dispatchers.IO) { TODO() }

    /**
     * Link the user account to the Google Provider
     */
    suspend fun linkWithGoogle() = withContext(Dispatchers.IO) { TODO() }
    /**
     * Link the user account to the Facebook Provider
     */
    suspend fun linkWithFacebook() = withContext(Dispatchers.IO) { TODO() }

    /**
     * Signs the current user out
     */
    suspend fun signout() = withContext(Dispatchers.IO) { TODO() }
}
