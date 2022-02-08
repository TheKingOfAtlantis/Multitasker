package uk.co.sksulai.multitasker.db.model

import java.util.*
import java.time.Duration

import androidx.room.*

/**
 * Specifies a rule defining the duration of time before the start of an event
 * to send a notification/reminder
 *
 * These can be applied to both calendars and events.
 *
 * When added to a calendar the rules are inherited by its events, changes made to
 * the calendar's rules are propagated to its events. Events can add rules to provide
 * additional reminders to the user however additional override rules can be specified
 * which target how the rules of a calendar are then used by an event
 *
 * @param notificationID Unique ID for the notification rule
 * @param duration       How long before the event to send reminders
 */
@Entity(tableName = "NotificationRule")
data class NotificationRuleModel(
    @PrimaryKey val notificationID: UUID,
    val duration: Duration
)

// Notification associations
/**
 * Represents a link between a calendar and its notification rules
 *
 * @param calendarID     ID of the calendar
 * @param notificationID ID of the notification rule
 */
@Entity(
    primaryKeys = ["calendarID", "notificationID"],
    foreignKeys = [
        ForeignKey(
            entity = CalendarModel::class,
            parentColumns = ["calendarID"],
            childColumns  = ["calendarID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = NotificationRuleModel::class,
            parentColumns = ["notificationID"],
            childColumns  = ["notificationID"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
) data class CalendarNotificationJunction(
    @ColumnInfo(index = true) val calendarID: UUID,
    @ColumnInfo(index = true) val notificationID: UUID
)
/**
 * Represents a link between an event and its notification rules.
 *
 * This table can also be used to specify calendar override rules by pointing to
 * the calendar's rule and specifying how it is overridden (currently can only
 * choose to ignore a calendar rule)
 *
 * @param eventID        ID of the [EventModel]
 * @param notificationID ID of the notification rule
 * @param overrides      Set of override rules to (optionally) apply to the rule
 */
@Entity(
    primaryKeys = ["eventID", "notificationID"],
    foreignKeys = [
        ForeignKey(
            entity = EventModel::class,
            parentColumns = ["eventID"],
            childColumns  = ["eventID"],
            onDelete = ForeignKey.CASCADE

        ),
        ForeignKey(
            entity = NotificationRuleModel::class,
            parentColumns = ["notificationID"],
            childColumns  = ["notificationID"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
) data class EventNotificationJunction(
    @ColumnInfo(index = true) val eventID: UUID,
    @ColumnInfo(index = true) val notificationID: UUID,
    @Embedded val overrides: NotificationOverride? = null
)
/**
 * Overrides rules which are applied to the event by its calendar
 * @param ignore Whether to ignore this rule
 */
data class NotificationOverride(
    val ignore: Boolean = false
)

/**
 * A calendar with its notification rules
 *
 * @param calendar The calendar
 * @param notificationRules List of the rules which apply to the calendar
 */
data class CalendarWithNotifications(
    @Embedded val calendar: CalendarModel,
    @Relation(
        entity = NotificationRuleModel::class,
        parentColumn = "calendarID",
        entityColumn = "notificationID",
        associateBy = Junction(
            CalendarNotificationJunction::class,
            parentColumn = "calendarID",
            entityColumn = "notificationID",
        )
    ) val notificationRules: List<NotificationRuleModel>
)
/**
 * A calendar with its notification rules
 *
 * @param event The event
 * @param notificationRules List of the rules which apply to the event
 */
data class EventWithNotifications(
    @Embedded val event: EventModel,
    @Relation(
        entity = NotificationRuleModel::class,
        parentColumn = "eventID",
        entityColumn = "notificationID",
        associateBy = Junction(
            EventNotificationJunction::class,
            parentColumn = "eventID",
            entityColumn = "notificationID",
        )
    ) val notificationRules: List<NotificationRuleModel>
)

