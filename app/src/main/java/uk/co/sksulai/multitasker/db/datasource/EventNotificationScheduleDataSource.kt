package uk.co.sksulai.multitasker.db.datasource

import java.util.*
import kotlinx.coroutines.flow.Flow
import uk.co.sksulai.multitasker.db.model.EventNotificationScheduleModel

interface EventNotificationScheduleDataSource {
    /**
     * Inserts the [schedule] for an event notification
     */
    suspend fun insert(vararg schedule: EventNotificationScheduleModel)
    /**
     * Updates the [schedule] for an event notification
     */
    suspend fun update(vararg schedule: EventNotificationScheduleModel)
    /**
     * Deletes the [schedule] for an event notification
     */
    suspend fun delete(vararg schedule: EventNotificationScheduleModel)

    /**
     * Retrieve all scheduled event notifications
     */
    fun getAll(): Flow<List<EventNotificationScheduleModel>>
    /**
     * Deletes all scheduled notifications
     */
    fun deleteAll()
    /**
     * Deletes all scheduled notifications which have been posted
     */
    fun deleteHaveOccurred()
    /**
     * Delete all scheduled notifications associated with an event
     * @param id The id of the event
     */
    fun deleteFromEvent(id: UUID)

    /**
     * Retrieve a schedule from its [alarmID]
     */
    fun fromID(alarmID: Int): Flow<EventNotificationScheduleModel?>
    /**
     * Retrieve the scheduled events for an event
     * @param id The id of the event
     */
    fun fromEvent(id: UUID): Flow<List<EventNotificationScheduleModel>>
    /**
     * Retrieve the schedule for a specific [notification] for an [event]
     */
    fun fromNotification(event: UUID, notification: UUID): Flow<EventNotificationScheduleModel?>
}
