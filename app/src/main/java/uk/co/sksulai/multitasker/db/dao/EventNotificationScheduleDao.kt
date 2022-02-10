package uk.co.sksulai.multitasker.db.dao

import java.util.*
import androidx.room.*
import kotlinx.coroutines.flow.Flow

import uk.co.sksulai.multitasker.db.model.EventNotificationScheduleModel
import uk.co.sksulai.multitasker.db.datasource.EventNotificationScheduleDataSource

@Dao interface EventNotificationScheduleDao : EventNotificationScheduleDataSource, DatabaseService {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    override suspend fun insert(vararg schedule: EventNotificationScheduleModel)
    @Update override suspend fun update(vararg schedule: EventNotificationScheduleModel)
    @Delete override suspend fun delete(vararg schedule: EventNotificationScheduleModel)

    @Query("Delete from EventSchedule") override fun deleteAll()
    @Query("Delete from EventSchedule where eventID = :id")
    override fun deleteFromEvent(id: UUID)

    @Query("Delete from EventSchedule where posted = 1")
    override fun deleteHaveOccurred()
    @Query("Select * From EventSchedule")
    override fun getAll(): Flow<List<EventNotificationScheduleModel>>

    @Query("Select * From EventSchedule Where alarmID = :alarmID")
    override fun fromID(alarmID: Int): Flow<EventNotificationScheduleModel?>
    @Query("Select * From EventSchedule Where eventID = :id")
    override fun fromEvent(id: UUID): Flow<List<EventNotificationScheduleModel>>
    @Query("Select * From EventSchedule Where (eventID = :event AND notificationID = :notification)")
    override fun fromNotification(event: UUID, notification: UUID): Flow<EventNotificationScheduleModel?>
}
