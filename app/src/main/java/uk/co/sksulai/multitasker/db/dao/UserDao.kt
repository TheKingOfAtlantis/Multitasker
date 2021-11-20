package uk.co.sksulai.multitasker.db.dao

import kotlinx.coroutines.flow.Flow

import androidx.room.*

import uk.co.sksulai.multitasker.db.model.UserModel
import uk.co.sksulai.multitasker.db.datasource.UserDataSource

@Dao interface UserDao : UserDataSource, DatabaseService {
    @Insert(onConflict = OnConflictStrategy.REPLACE) override suspend fun insert(user: UserModel)
    @Update override suspend fun update(user: UserModel)
    @Delete override suspend fun delete(user: UserModel)

    @Query("Delete From UserModel") suspend fun deleteAll() : Int
    @Query("Select * From UserModel") fun getAll() : Flow<List<UserModel>>

    @Query("Select * From UserModel Where ID is :id") override fun fromID(id: String) : Flow<UserModel?>
    @Query("Select * From UserModel Where DisplayName like :displayName") override fun fromDisplayName(displayName: String) : Flow<List<UserModel>>
    @Query("Select * From UserModel Where ActualName like :actualName") override fun fromActualName(actualName: String) : Flow<List<UserModel>>
}
