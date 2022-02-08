package uk.co.sksulai.multitasker.db.datasource

import java.util.*
import kotlinx.coroutines.flow.Flow
import uk.co.sksulai.multitasker.db.model.*

interface NotificationRuleDataSource {
    /**
     * Inserts notification rule(s)
     * @param rule The notification(s) to add
     */
    suspend fun insert(vararg rule: NotificationRuleModel)
    /**
     * Updates notification rule(s)
     * @param rule The notification(s) to update
     */
    suspend fun update(vararg rule: NotificationRuleModel)
    /**
     * Deletes notification rule(s)
     * @param rule The notification(s) to remove
     */
    suspend fun delete(vararg rule: NotificationRuleModel)

    /**
     * Creates a link between an event and the notification rule
     *
     * @param event The event
     * @param rule  The rule to associate with [event]
     */
    suspend fun associate(event: EventModel, vararg rule: NotificationRuleModel)
    /**
     * Creates a link between a calendar and the notification rule
     *
     * @param calendar The event
     * @param rule     The rule to associate with [calendar]
     */
    suspend fun associate(calendar: CalendarModel, vararg rule: NotificationRuleModel)
    /**
     * Creates a rule override for a particular event
     *
     * @param event     The event which is overriding the rule
     * @param rule      The rule to be overridden
     * @param overrides Overrides to apply to notification [rule] for [event]
     */
    suspend fun override(
        event: EventModel,
        rule: NotificationRuleModel,
        overrides: NotificationOverride
    )
    /**
     * Retrieves the overrides associated with an event
     * @param eventID ID of the event
     * @return Map of [NotificationRuleModel.notificationID] with the associated [NotificationOverride]
     */
    fun overridesOf(eventID: UUID): Flow<Map<UUID, NotificationOverride>>

    /**
     * Retrieves a notification rule given its ID
     * @param id The id of the notification
     */
    fun fromID(id: UUID): Flow<NotificationRuleModel?>
    /**
     * Retrieves a calendar with the rules associated with it
     * @param id The id of the calendar
     */
    fun fromCalendar(id: UUID): Flow<CalendarWithNotifications?>
    /**
     * Retrieves an event with the rules associated with it (not including the calendar rules)
     * @param id The id of the event
     */
    fun fromEvent(id: UUID): Flow<EventWithNotifications?>
}
