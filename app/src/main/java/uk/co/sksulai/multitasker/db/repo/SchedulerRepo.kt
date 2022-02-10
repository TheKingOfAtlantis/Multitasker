package uk.co.sksulai.multitasker.db.repo

import java.time.*
import java.util.*

import javax.inject.Inject

import kotlinx.coroutines.flow.*
import androidx.room.withTransaction

import uk.co.sksulai.multitasker.db.LocalDB
import uk.co.sksulai.multitasker.db.model.*
import uk.co.sksulai.multitasker.db.dao.*

/**
 * Used to handle the logic behind adding, updating and removing schedule
 *
 * @param db The database storing the data
 * @param eventScheduleDao DAO used to access the database
 */
class SchedulerRepo @Inject constructor(
    private val db: LocalDB,
    private val eventScheduleDao: EventNotificationScheduleDao,
    private val calendarRepo: CalendarRepo
) {
    suspend fun <R> withTransaction(block: suspend () -> R) = db.withTransaction(block)

    /**
     * Generate the alarm IDs
     */
    private fun generateAlarmID() = generateID().hashCode()

    /**
     * Schedules an event notification given the [event] and notification [rule].
     * It calculates when the event should occur as well as generating an ID to
     * use to identify the alarm.
     *
     * @return The [EventNotificationScheduleModel] which was created and inserted
     *         into the database
     */
    suspend fun createEventNotification(
        event: EventModel,
        rule: NotificationRuleModel
    ) = createEventNotification(
        alarm = generateAlarmID(),
        event = event.eventID,
        rule  = rule.notificationID,
        time  = (event.start - rule.duration).toInstant(),
        start = event.start,
        reminder = rule.duration
    )
    /**
     * Create and inserts [EventNotificationScheduleModel]
     */
    private suspend fun createEventNotification(
        alarm: Int,
        event: UUID,
        start: ZonedDateTime,
        rule: UUID,
        reminder: Duration,
        time: Instant
    ) = create(
        EventNotificationScheduleModel(
            alarmID = alarm,
            eventID = event,
            notificationID = rule,
            scheduledTime = time,
            posted = false,
            start = start,
            reminder = reminder
        )
    )
    private suspend fun create(schedule: EventNotificationScheduleModel) =
        schedule.also { insert(schedule) }

    /**
     * Marks that a particular notification has been posted
     * @param alarmID The alarm ID associated with the particular event notification
     */
    suspend fun markPosted(alarmID: Int) = withTransaction {
        fromAlarmID(alarmID).first()?.let { markPosted(it) }
    }
    /**
     * Marks that a particular notification has been posted
     * @param schedule The schedule associated with the particular event notification
     */
    suspend fun markPosted(schedule: EventNotificationScheduleModel) =
        update(schedule.copy(posted = true))

    /**
     * Updates the scheduling of an alarm.
     *
     * If the event or notification are found to have been deleted then the scheduling entry
     * is also deleted and the caller should then ensure that the alarm is cancelled rather
     * than updated.
     *
     * @return The new [EventNotificationScheduleModel] or null if the event/notification
     *         were found to have been deleted. Will also return null if the alarm ID is
     *
     */
    suspend fun update(alarmID: Int): EventNotificationScheduleModel? {
        // We already know the ID of the notification and event that we are looking for
        // so we can just pull the latest info from the database and use that

        val entry = fromAlarmID(alarmID).first()
        entry?.let {
            val event        = calendarRepo.getEventFrom(entry.eventID).first()
            val notification = calendarRepo.getNotificationRuleFrom(entry.notificationID).first()

            return if(event != null && notification != null) {
                // Neither the event nor the notification have been deleted

                // We need to update when it is scheduled for
                // and cache the new start and reminder offset
                 entry.copy(
                    scheduledTime = (event.start - notification.duration).toInstant(),
                    start         = event.start,
                    reminder      = notification.duration
                ).also { update(it) }
            } else {
                // Either the event nor the notification has been deleted
                // so we have to delete the scheduling
                delete(entry)
                null
            }
        } ?: throw IllegalArgumentException("Cannot update an non-existent scheduling entry")
    }

    /**
     * Inserts a [schedule] for a notification of an event
     */
    suspend fun insert(vararg schedule: EventNotificationScheduleModel) =
        eventScheduleDao.insert(*schedule)
    /**
     * Updates the [schedule] for a notification of an event
     */
    suspend fun update(vararg schedule: EventNotificationScheduleModel) =
        eventScheduleDao.update(*schedule)
    /**
     * Removes a [schedule] for a notification of an event
     */
    suspend fun delete(vararg schedule: EventNotificationScheduleModel) =
        eventScheduleDao.update(*schedule)

    /**
     * Removes all scheduling for notifications which have been posted
     */
    suspend fun prune() {
        eventScheduleDao.deletePosted()
    }

    /**
     * Retrieves the schedule associated with [alarmID]
     * @param alarmID The ID which was given to the alarm
     */
    fun fromAlarmID(alarmID: Int) = eventScheduleDao.fromID(alarmID)
    /**
     * Retrieves all scheduled notifications for an [event]
     */
    fun fromEvent(event: EventModel) = eventScheduleDao.fromEvent(event.eventID)
    /**
     * Retrieves the scheduling associated with a particular [notification] of an [event]
     */
    fun fromNotification(event: EventModel, notification: NotificationRuleModel) =
        fromNotification(event.eventID, notification.notificationID)
    /**
     * Retrieves the scheduling associated with a particular [notification] of an [event]
     */
    fun fromNotification(event: UUID, notification: UUID) = eventScheduleDao.fromNotification(event, notification)

}
