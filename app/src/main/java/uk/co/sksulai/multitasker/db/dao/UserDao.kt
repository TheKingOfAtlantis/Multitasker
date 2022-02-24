package uk.co.sksulai.multitasker.db.dao

import kotlinx.coroutines.flow.Flow

import androidx.room.*

import uk.co.sksulai.multitasker.db.model.UserModel
import uk.co.sksulai.multitasker.db.datasource.UserDataSource

@Dao interface UserDao : UserDataSource, DatabaseService {
    /**
     * Insert a user into the database (replacing if it already exists)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE) override suspend fun insert(user: UserModel)

    /**
     * Updates a pre-existing user in the database
     */
    @Update override suspend fun update(user: UserModel)
    /**
     * Removes a user from the database
     */
    @Delete override suspend fun delete(user: UserModel)

    /**
     * Deletes all users stored in the database
     */
    @Query("Delete From UserModel") suspend fun deleteAll() : Int
    /**
     * Retrieves a list of the users in the database
     */
    @Query("Select * From UserModel") override fun getAll() : Flow<List<UserModel>>

    /**
     * Retrieve a user given their unique ID
     * @param id The ID of the user to retrieve
     * @return A flow of the associated user (which can return null if no user was found)
     */
    @Query("Select * From UserModel Where ID is :id")
    override fun fromID(id: String) : Flow<UserModel?>
    /**
     * Retrieves a list of users with a display name
     * @param displayName The display name to use for the query
     * @return A flow containing a list of all the users which were found
     */
    @Query("Select * From UserModel Where DisplayName like :displayName")
    override fun fromDisplayName(displayName: String) : Flow<List<UserModel>>
    /**
     * Retrieves a list of users with a given name
     * @param actualName The name to use for the query
     * @return A flow containing a list of all the users which were found
     */
    @Query("Select * From UserModel Where ActualName like :actualName")
    override fun fromActualName(actualName: String) : Flow<List<UserModel>>
}
