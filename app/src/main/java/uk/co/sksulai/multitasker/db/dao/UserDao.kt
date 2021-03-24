package uk.co.sksulai.multitasker.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import uk.co.sksulai.multitasker.db.model.UserModel

@Dao interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(value: UserModel)
    @Update suspend fun update(value: UserModel)
    @Delete suspend fun delete(value: UserModel)

    @Query("Delete From UserModel") suspend fun deleteAll() : Int
    @Query("Select * From UserModel") fun getAll() : Flow<List<UserModel>>

    @Query("Select * From UserModel Where ID is :id") fun fromID(id: String) : UserModel?
    @Query("Select * From UserModel Where DisplayName like :displayName") fun fromDisplayName(displayName: String) : Flow<List<UserModel>>
    @Query("Select * From UserModel Where ActualName like :actualName") fun fromActualName(actualName: String) : Flow<List<UserModel>>
}
