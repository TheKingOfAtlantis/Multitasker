package uk.co.sksulai.multitasker.db.repo

import java.time.*
import java.util.*

import javax.inject.Inject

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

import androidx.room.withTransaction

import uk.co.sksulai.multitasker.db.LocalDB
import uk.co.sksulai.multitasker.db.dao.*
import uk.co.sksulai.multitasker.db.model.*

class CalendarRepo @Inject constructor(
    private val db: LocalDB,
    private val calendarDao: CalendarDao,
    private val eventDao: EventDao,
    private val tagDao: TagDao,
    private val notificationDao: NotificationRuleDao
) {
    // Creation
    
    suspend fun <R> withTransaction(block: suspend () -> R) = db.withTransaction(block)

    /**
     * Creates a calendar
     *
     * @param owner         The owner of this calendar
     * @param name          The name of this calendar
     * @param description   Description of this calendar
     * @param colour        The colour of this calendar
     * @param reminders The set of notification rules to apply to this calendar
     *
     * @return [CalendarModel] instance of the newly created calendar
     */
    suspend fun createCalendar(
        owner: UserModel,
        name: String,
        description: String,
        colour: Color,
        reminders: List<Duration>
    ) = createCalendar(
        owner       = owner.userID,
        name        = name,
        description = description,
        colour      = colour,
        reminders   = reminders,
    )
    /**
     * Creates a calendar
     *
     * @param owner       The owner of this calendar
     * @param name        The name of this calendar
     * @param description A description of this calendar
     * @param colour      The colour of this calendar
     *
     * @return [CalendarModel] instance of the newly created calendar
     */
    suspend fun createCalendar(
        owner: String,
        name: String,
        description: String,
        colour: Color,
        reminders: List<Duration>
    ) = withTransaction {
        create(CalendarModel(
            calendarID  = generateID(),
            ownerID     = owner,
            name        = name,
            description = description,
            colour      = colour.toArgb(),
            visible     = true
        )).also { notificationDao.associate(it, *createNotificationRules(reminders).toTypedArray()) }
    }

    /**
     * Creates a event with a set of tags
     *
     * @param calendar    The calendar to add the event to
     * @param name        The name of the event
     * @param description Description of the event
     * @param allDay      Whether the event is an all-day event
     * @param start       When the event starts
     * @param duration    How long the event lasts
     * @param endTimeZone The timezone in which the event ends
     * @param reminders   List of reminder offsets
     * @param colour      Optional colour to associated with the event
     * @param category    Category to associate with the event
     * @param tags        Tags associated with the event
     * @param parentID    ID of the parent event
     *
     * @return [EventWithTags] instance of the newly created event and tags
     */
    suspend fun createEvent(
        calendar: CalendarModel,
        name: String,
        description: String,
        allDay: Boolean,
        start: ZonedDateTime,
        duration: Duration,
        endTimeZone: TimeZone,
        reminders: List<Duration> = emptyList(),
        colour: Color?         = null,
        category: String       = "",
        location: String       = "",
        tags: List<String>     = emptyList(),
        parentID: UUID?        = null,
    ) = withTransaction {
        EventWithTags(createEvent(
            calendar,
            name,
            description,
            allDay, start, duration, endTimeZone,
            reminders,
            colour, category, location,
            parentID
        ), createTags(tags)).also { (event, tags) -> tagDao.associate(event, tags) }
    }
    /**
     * Creates a event with reminders
     *
     * @param calendar    The calendar to add the event to
     * @param name        The name of the event
     * @param description Description of the event
     * @param allDay      Whether the event is an all-day event
     * @param start       When the event starts
     * @param duration    How long the event lasts
     * @param endTimeZone The timezone in which the event ends
     * @param reminders   List of reminder offsets
     * @param colour      Optional colour to associated with the event
     * @param category    Category to associate with the event
     * @param parentID    ID of the parent event
     *
     * @return [EventModel] instance of the newly created event
     */
    suspend fun createEvent(
        calendar: CalendarModel,
        name: String,
        description: String,
        allDay: Boolean,
        start: ZonedDateTime,
        duration: Duration,
        endTimeZone: TimeZone,
        reminders: List<Duration>,
        colour: Color?   = null,
        category: String = "",
        location: String = "",
        parentID: UUID?  = null,
    ) = withTransaction {
        createEvent(
            calendarId  = calendar.calendarID,
            name        = name,
            description = description,
            allDay      = allDay,
            start       = start,
            duration    = duration,
            endTimeZone = endTimeZone,
            colour      = colour?.toArgb(),
            category    = category,
        	location    = location,
            parentID    = parentID,
        ).also { updateAssociatedReminders(it, reminders) }
    }
    /**
     * Creates a event
     *
     * @param calendarId  The calendar to add the event to
     * @param name        The name of the event
     * @param description Description of the event
     * @param allDay      Whether the event is an all-day event
     * @param start       When the event starts
     * @param duration    How long the event lasts
     * @param endTimeZone The timezone in which the event ends
     * @param colour      Optional colour to associated with the event
     * @param category    Category to associate with the event
     * @param parentID    ID of the parent event
     *
     * @return [EventModel] instance of the newly created event
     */
    suspend fun createEvent(
        calendarId: UUID,
        name: String,
        description: String,
        allDay: Boolean,
        start: ZonedDateTime,
        duration: Duration,
        endTimeZone: TimeZone,
        colour: Int?     = null,
        category: String = "",
        location: String = "",
        parentID: UUID?  = null,
    ) = create(EventModel(
        eventID     = generateID(),
        calendarID  = calendarId,
        parentID    = parentID,
        name        = name,
        description = description,
        colour      = colour,
        category    = category,
        location    = location,
        allDay      = allDay,
        start       = start,
        duration    = duration,
        endTimezone = endTimeZone
    ))

    /**
     * Creates tags from a list of content values unless a tag already exists in which case the tag
     * containing that value is retrieved
     * @param tags List containing the requested tag contents
     * @return List of [EventTagModel]s which have been added to the database
     */
    suspend fun createTags(tags: List<String>): List<EventTagModel> = withTransaction {
        // Get list of existing tags
        // Remove them from the given list to get those we need to create
        val exist = getTagFrom(tags).first()
        val result = create(
            tags.filter { check -> exist.find { it.content == check } == null }
                .map { EventTagModel(generateID(), it) }
        ) + exist

        // Just for niceness return the list in the same order as it was given
        val order = tags.mapIndexed { i, s -> s to i }.toMap()
        result.sortedBy { it.content.let(order::getValue) }
    }

    suspend fun createNotificationRules(notifications: List<Duration>): List<NotificationRuleModel> =
        create(notifications.map { NotificationRuleModel(generateID(), it) })

    private suspend fun create(calendar: CalendarModel)   = calendar.also { insert(it) }
    private suspend fun create(event: EventModel)         = event.also { insert(it) }
    @JvmName("create_tags")
    private suspend fun create(tags: List<EventTagModel>) = tags.also { insert(*it.toTypedArray()) }
    @JvmName("create_notifications")
    private suspend fun create(notifications: List<NotificationRuleModel>) = notifications.also { insert(*it.toTypedArray()) }

    // Manipulation

    /**
     * Inserts a calendar into the database
     * @param calendar The calendar that is to be added
     */
    suspend fun insert(calendar: CalendarModel): Unit = withTransaction { calendarDao.insert(calendar) }
    /**
     * Inserts an event into the database
     * @param event The calendar that is to be added
     */
    suspend fun insert(event: EventModel): Unit = withTransaction { eventDao.insert(event) }
    
    /**
     * Inserts a number of tags into the database
     * @param tags The tags that are to be added
     */
    suspend fun insert(vararg tags: EventTagModel): Unit = withTransaction { tagDao.insert(*tags) }
    /**
     * Inserts a number of notification rules into the database
     * @param rules The rules that are to be added
     */
    suspend fun insert(vararg notifications: NotificationRuleModel): Unit = withTransaction { notificationDao.insert(*notifications) }
    /**
     * Updates a calendar
     * @param calendar Calendar model which has been modified
     */
    suspend fun update(calendar: CalendarModel) = withTransaction { calendarDao.update(calendar) }
    /**
     * Updates an event
     * @param event Event model which has been modified
     */
    suspend fun update(event: EventModel) = withTransaction { eventDao.update(event) }

    /**
     * Updates the list of tags associated with an event
     * @param event The event to modify
     * @param tags  List of tags to be added to the event
     */
    suspend fun updateAssociatedTags(event: EventModel, tags: List<String>) = db.withTransaction {
        // Convert the list of given tags to EventTagModel
        // Create tags which don't exist yet and retrieve those which do
        val tagModels = createTags(tags) // Luckily createTags does all of that
        // Get the current list of tags associated with the event
        updateAssociatedTags(getEventWithTags(event.eventID).first()!!, tagModels)
    }

    private suspend fun updateAssociatedTags(event: EventWithTags, tags: List<EventTagModel>) = db.withTransaction {
        // Work out which are tags are new and which have been removed from the event
        // If no more events are associated with a tag remove the tag

        // First find the new tags to add
        tags.filterNot(event.tags::contains)
            .let { tagDao.associate(event.event, it) }
        // Then find the old tags to remove
        event.tags                                         // Take the list tags in the event
            .filterNot(tags::contains)                     // Leave those not contained in the list of given tags
            .also { tagDao.disassociate(event.event, it) } // Disassociate from the tags
        // Lastly clean up the tags
        // We can reuse the list of disassociated tags to reduce the amount of work
            .map { tagDao.withTag(it.tagID).first() }   // Get each tag with the events which use it
            .filter { (_, events) -> events.isEmpty() } // If the tag has not events associated with it
            .map(EventsWithTag::tag)                    // Then get the tag
            .let { delete(it) }                         // and delete it
    }

    /**
     * Updates the list of notification rules associated with a [calendar]
     * @param rules The new list of notification offsets
     */
    suspend fun updateAssociatedReminders(calendar: CalendarModel, rules: List<Duration>) = withTransaction {
        updateAssociatedReminders(
            notificationDao.fromCalendar(calendar.calendarID).first()!!,
            rules
        )
    }
    /**
     * Updates the list of notification rules associated with a calendar
     * @param calendarWithNotifications A calendar with its current notification rules
     * @param rules The new list of notification offsets
     */
    suspend fun updateAssociatedReminders(calendarWithNotifications: CalendarWithNotifications, rules: List<Duration>) = withTransaction {
        with(calendarWithNotifications) {
            // Remove any rules which are in currently associated with the calendar but not in new rules
            val notifications = notificationRules.associateWith { it.duration }
            notifications
                .filterValues { it !in rules }
                .let { delete(it.keys.toList()) }
            // Remove any rules which are in not associated with the calendar but are in new rules
            createNotificationRules(
                rules.filter { it !in notifications.values }
            )
        }
    }
    /**
     * Updates the list of notification rules associated with an [event]
     * @param rules The new list of notification offsets
     */
    suspend fun updateAssociatedReminders(event: EventModel, rules: List<Duration>) = withTransaction {
        updateAssociatedReminders(
            notificationDao.fromEvent(event.eventID).first()!!,
            rules
        )
    }
    /**
     * Updates the list of notification rules associated with an event
     * @param eventWithNotifications An event with its current notification rules
     * @param rules The new list of notification offsets
     */
    suspend fun updateAssociatedReminders(eventWithNotifications: EventWithNotifications, rules: List<Duration>) = withTransaction {
        // First we need to ensure that we don't add rules already covered by the calendar
        // and any rules covered by the calendar but removed by the event are marked as ignore

        val event = eventWithNotifications.event
        val calendar = notificationDao.fromCalendar(event.calendarID).first()!!

        val eventRules = eventWithNotifications.notificationRules.associateWith { it.duration }
        val calendarRules = calendar.notificationRules.associateWith { it.duration }

        // Ignore calendar rules
        calendarRules
            // Get calendar rules not in the event rules
            .filterValues { it !in rules }
            // Mark them as ignore
            .forEach { (rule, _) -> notificationDao.override(event, rule, NotificationOverride(true)) }

        // Get list of those not contained in calendar rules
        rules.filterNot { calendarRules.containsValue(it) }.let { notifications ->
            // Now we have list of the events reminders we need to check against what it already has
            notifications.filterNot { eventRules.containsValue(it) }.let {
                // Create the rules and associate them with the event
                val reminders = createNotificationRules(it)
                notificationDao.associate(event, *reminders.toTypedArray())
            }
            // Also need to check for values the old list contains but have been removed
            delete(
                eventRules
                    .filterValues { it !in notifications }
                    .keys.toList()
            )
        }
    }

    /**
     * Deletes a calendar from the database
     * @param calendar Calendar to be removed
     */
    suspend fun delete(calendar: CalendarModel) = withTransaction {
        // Need to ensure we don't leave notification rules behind before deleting
        getNotificationRulesFor(calendar).first()?.let { delete(it.notificationRules) }
        calendarDao.delete(calendar)
    }
    /**
     * Deletes an event from the database
     * @param event Event to be removed
     */
    suspend fun delete(event: EventModel) = withTransaction {
        // Need to ensure that we remove tags and notification rules before deleting
        // to ensure nothing is left behind
        getEventWithTags(event.eventID).first()?.let { delete(it.tags) }
        getNotificationRulesFor(event).first()?.let { delete(it.notificationRules) }
        eventDao.delete(event)
    }
    /**
     * Deletes tags from the database
     * @param tags Tags to be removed
     */
    @JvmName("delete_tags")
    suspend fun delete(tags: List<EventTagModel>) = withTransaction { tagDao.delete(*tags.toTypedArray()) }
    /**
     * Deletes notification rules from the database
     * @param tags Tags to be removed
     */
    @JvmName("delete_notifications")
    suspend fun delete(rules: List<NotificationRuleModel>) = withTransaction {
        notificationDao.delete(*rules.toTypedArray())
    }

    // Getters

    /**
     * List of all the locally stored calendars
     */
    val calendars = calendarDao.getAll()

    /**
     * Retrieves a calendar given an ID
     * @param id The ID to be queried
     * @return Flow to the calendar if it was found
     */
    fun getCalendarFrom(id: UUID) = calendarDao.fromID(id)
    /**
     * Retrieves the calendar which an event is a part of
     * @param event The event to find the calendar for
     * @return Flow to the calendar
     */
    fun getCalendarFrom(event: EventModel) = calendarDao.fromID(event.calendarID)
    /**
     * Retrieves a list of calendars which match the name
     * @param name       Query with the name of the calendar
     * @param queryParam Query parameters to apply to the search
     * @return Flow to the list of calendars found
     */
    fun getCalendarFrom(name: String, queryParam: QueryBuilder.() -> Unit = {}) =
        calendarDao.fromName(SearchQuery.local(name, queryParam))

    /**
     * List of all the locally stored events
     */
    val events get() = eventDao.getAllWithCalendar()

    /**
     * Retrieves an event given an ID
     * @param id The ID to be queried
     * @return Flow to the event found
     */
    fun getEventFrom(id: UUID) = eventDao.fromID(id)
    /**
     * Retrieves a list of events contained by a calendar
     * @param calendar The calendar to be queried
     * @return Flow to the list of events found
     */
    fun getEventFrom(calendar: CalendarModel) = eventDao.fromCalendar(calendar.calendarID)
    /**
     * Retrieves a list of events which match a name
     * @param name       The name of the event
     * @param queryParam Query parameters to apply to the search
     * @return Flow to the list of events found
     */
    fun getEventFrom(name: String, queryParam: QueryBuilder.() -> Unit = {}) =
        eventDao.fromName(SearchQuery.local(name, queryParam))
    /**
     * Retrieve a list of events with a given tag
     * @param tag The tag the events need to contain
     * @return [EventsWithTag] contains the list of events along with the tag
     */
    fun getEventFrom(tag: EventTagModel) = tagDao.withTag(tag.tagID)
    /**
     * Retrieve the list of tags associated with the
     * @param id The event to be queried ([EventModel.eventID])
     * @return [EventWithTags] contains the list of tags along with the event itself
     */
    fun getEventWithTags(id: UUID) = tagDao.forEvent(id)
    /**
     * Retrieve an event with its associated calendar
     * @param id The id of event to be queried
     */
    fun getEventWithCalendar(id: UUID) = eventDao.withCalendar(id)
    /**
     * Retrieve an event with its children
     * @param id The id of event to be queried
     */
    fun getEventWithChildren(id: UUID) = eventDao.withChildren(id)
    /**
     * Retrieve an event with its children
     * @param event The event to be queried
     */
    fun getEventWithChildren(event: EventModel) = getEventWithChildren(event.eventID)

    /**
     * List of all the locally available tags
     */
    val tags get() = tagDao.getAll()

    /**
     * Retrieves a tag given an ID
     * @param id The ID to be queried
     */
    fun getTagFrom(id: UUID) = tagDao.fromID(id)
    /**
     * Retrieves a list tags which whose contents match the given query
     * @param content    The content of the tag
     * @param queryParam Query parameters to apply to the search
     */
    fun getTagFrom(content: String, queryParam: QueryBuilder.() -> Unit = {}) =
        tagDao.fromContent(SearchQuery.local(content, queryParam))
    /**
     * Retrieves a list of tags which match any of the given contents
     * @param contents   List of contents to search for
     * @param queryParam Query parameters to apply to the search
     * @return Flow containing a list of tags with a match
     */
    fun getTagFrom(contents: List<String>, queryParam: QueryBuilder.() -> Unit = {}) = flow {
        if(contents.isEmpty()) emit(emptyList())
        else emitAll(tagDao.fromContent(contents.map { SearchQuery.local(it, queryParam) }))
    }

    /**
     * Retrieves the notification rule given its [notificationID]
     */
    fun getNotificationRuleFrom(notificationID: UUID) = notificationDao.fromID(notificationID)
    /**
     * Retrieves the notification rules which are associated with a [calendar]
     */
    fun getNotificationRulesFor(calendar: CalendarModel) = notificationDao.fromCalendar(calendar.calendarID)
    /**
     * Retrieves the notification rules which are associated with an [event]
     * _n.b._ Does not retrieve those associated with the calendar
     */
    fun getNotificationRulesFor(event: EventModel) = notificationDao.fromEvent(event.eventID)
    /**
     * Retrieves the notfication override rules for a given [event]
     */
    fun getNotificationOverridesFor(event: EventModel) = notificationDao.overridesOf(event.eventID)

    /**
     * Determines the notifications which applies to an [event]; given its own
     * notification rules, the rules of the calendar and the overrides specified
     * by the event
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun determineNotificationsOf(event: EventModel) =
        getEventWithCalendar(event.eventID)
            .filterNotNull()
            .flatMapLatest { determineNotificationsOf(it) }
    /**
     * Determines the notifications which applies to an [eventWithCalendar]; given
     * the events notification rules, the rules of the calendar and the overrides
     * specified by the event
     */
    fun determineNotificationsOf(eventWithCalendar: EventWithCalendar) = with(eventWithCalendar) {
        // We are guaranteed results since both should not be null
        val calendarRules = getNotificationRulesFor(calendar).filterNotNull()
        val eventRules    = getNotificationRulesFor(event).filterNotNull()
        val overrideRules = getNotificationOverridesFor(event)

        /**
         * Applies overrides to the calendar rules
         *
         * @param calendarRules List of rules from the calendar which could apply
         *                      to this event
         * @param overrideRules Rules specified by the event about how it overrides
         *                      the application of the calendar rules to itself
         * @return The [calendarRules] with the [overrideRules] applied to them
         */
        fun applyOverrides(
            calendarRules: List<NotificationRuleModel>,
            overrideRules: Map<UUID, NotificationOverride>
        ) = calendarRules.filter {
            if(it.notificationID in overrideRules.keys) {
                val rules = overrideRules.getValue(it.notificationID)
                !rules.ignore
            } else true
        }

        combine(calendarRules, eventRules, overrideRules) { calendar, event, overrides ->
            when {
                // If the event has no rules then we just use those that come with the calendar
                event.notificationRules.isEmpty() -> calendar.notificationRules
                // If the calendar has not rules then just use those that come with the event
                calendar.notificationRules.isEmpty() -> event.notificationRules
                else -> {
                    // If both have rules then we need to combine the two together
                    // However, we need to ensure that apply overrides associated with the event
                    event.notificationRules + applyOverrides(
                        calendar.notificationRules,
                        overrides
                    )
                }
            }
        }
    }

    /**
     * Retrieves all events which occur after a given [dateTime]
     */
    fun getEventAfter(dateTime: ZonedDateTime) = events.map {
        it.map { it.event }.filter { it.start.isAfter(dateTime) }
    }
    /**
     * Retrieves all events which before after a given [dateTime]
     */
    fun getEventBefore(dateTime: ZonedDateTime) = events.map {
        it.map { it.event }.filter { it.start.isBefore(dateTime) }
    }
}
