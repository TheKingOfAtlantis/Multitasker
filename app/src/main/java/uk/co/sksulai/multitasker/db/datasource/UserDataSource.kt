package uk.co.sksulai.multitasker.db.datasource

import kotlinx.coroutines.flow.Flow
import uk.co.sksulai.multitasker.db.model.UserModel

interface UserDataSource {
    suspend fun insert(user: UserModel)
    suspend fun update(user: UserModel)
    suspend fun delete(user: UserModel)

    fun getAll(): Flow<List<UserModel>>

    fun fromID(id: String): Flow<UserModel?>
    fun fromDisplayName(displayName: String): Flow<List<UserModel>>
    fun fromActualName(actualName: String): Flow<List<UserModel>>
}
