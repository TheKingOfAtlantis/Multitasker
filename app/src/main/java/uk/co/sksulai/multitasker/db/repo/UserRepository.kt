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
    suspend fun fromID(id: String): UserModel? = withContext(Dispatchers.IO) { TODO() }
    suspend fun fromFirebase(user: FirebaseUser): UserModel? = withContext(Dispatchers.IO) { TODO() }
    fun fromDisplayName(name: String): Flow<List<UserModel>> = withContext(Dispatchers.IO) { TODO() }
    fun fromActualName(name: String): Flow<List<UserModel>> = withContext(Dispatchers.IO) { TODO() }

    // Creation

    suspend fun create(user: FirebaseUser): UserModel = withContext(Dispatchers.IO) { TODO() }
    suspend fun create(
        id: String = generateID(),
        firebaseId: String? = null,
        displayName: String = "",
        email: String? = null,
        avatar: Uri? = null,
        actualName: String? = null,
        homeLocation: String? = null,
        dob: LocalDate? = null
    ): UserModel = withContext(Dispatchers.IO) { TODO() }
    suspend fun create(model: UserModel): UserModel  = withContext(Dispatchers.IO) { TODO() }

    // Update

    /**
     * Updates the users information
     * @param model - UserModel with the modifications to make
     */
    suspend fun update(model: UserModel) = withContext(Dispatchers.IO) { TODO() }

    // Delete

    /**
     * Deletes the user given an ID
     * @param id ID of the user to be deleted (UserModel#ID)
     * @param localOnly Whether to just delete from local database or propagate to Firebase
     */
    suspend fun delete(id: String, localOnly: Boolean) = withContext(Dispatchers.IO) { TODO() }
    /**
     * Deletes the user given the UserModel
     * @param id ID of the user to be deleted (UserModel#ID)
     * @param localOnly Whether to just delete from local database or propagate to Firebase
     */
    suspend fun delete(model: UserModel, localOnly: Boolean) = withContext(Dispatchers.IO) { TODO() }
    /**
     * Deletes the user given the Firebase user object
     * @param id ID of the user to be deleted (UserModel#ID)
     * @param localOnly Whether to just delete from local database or propagate to Firebase
     */
    suspend fun delete(model: FirebaseUser, localOnly: Boolean) = withContext(Dispatchers.IO) { TODO() }

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
