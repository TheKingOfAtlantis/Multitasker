package uk.co.sksulai.multitasker.db.model

import java.time.*
import java.util.*

import androidx.room.*

/**
 * Represents the scheduled time for a particular notification for an event
 *
 * To ensure that a reminder/notification associated for an event is correct we need to
 * keep track of the id given to the alarms which ensure notifications are sent exactly
 * as requested by the user.
 *
 * If the start of the event or its associated notifications are changed we need to
 * ensure that we update the alarms to avoid waking unnecessarily and sending a
 * notification when it is no longer correct to send it.
 *
 * To do so we keep track of the ID given to the alarm as well as the event/notification
 * associated with that particular alarm. We also keep track of when it scheduled and
 * [start] and [reminder] duration offset used to determine it (these are used to also
 * determine if the scheduled time needs to change).
 *
 * We also keep track of if a notification has [posted], which is used to ensure notifications
 * are sent following a reboot and to ensure we only prune those which have been sent
 *
 * @param alarmID        ID given to the alarm so we can modify it
 * @param eventID        ID of the event
 * @param notificationID ID of the notification rule
 * @param scheduledTime  The time the notification is scheduled for
 * @param posted         Indicates that the notification has been sent
 * @param start          Cached value of the event's start when created/last modified
 * @param reminder       Cached value of the rule's duration when created/last modified
 */
@Entity(
    tableName = "EventSchedule",
    foreignKeys = [
        // Even if the original entries are deleted we want to keep the schedule entry so that
        // we know what alarms to delete/ignore rather than not having any information
        ForeignKey(
            entity        = EventModel::class,
            parentColumns = ["eventID"],
            childColumns  = ["eventID"],
            onDelete      = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity        = NotificationRuleModel::class,
            parentColumns = ["notificationID"],
            childColumns  = ["notificationID"],
            onDelete      = ForeignKey.NO_ACTION
        ),
    ]
) data class EventNotificationScheduleModel(
    @PrimaryKey val alarmID: Int,
    @ColumnInfo(index = true) val eventID: UUID,
    @ColumnInfo(index = true) val notificationID: UUID,
    val scheduledTime: Instant,
    val posted: Boolean,
    val start: ZonedDateTime,
    val reminder: Duration
)
