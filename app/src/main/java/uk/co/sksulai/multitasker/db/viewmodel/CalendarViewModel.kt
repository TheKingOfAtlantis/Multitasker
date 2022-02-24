package uk.co.sksulai.multitasker.db.viewmodel

import android.app.Application
import java.util.*
import java.time.*

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

import javax.inject.Inject
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

import androidx.work.WorkManager

import uk.co.sksulai.multitasker.util.*
import uk.co.sksulai.multitasker.db.model.*
import uk.co.sksulai.multitasker.db.repo.CalendarRepo
import uk.co.sksulai.multitasker.notification.Notification
import uk.co.sksulai.multitasker.service.NotificationScheduler.Companion.startOneOffScheduling

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel class CalendarViewModel @Inject constructor(
    application: Application,
    private val calendarRepo: CalendarRepo,
    private val workManager: WorkManager
) : AndroidViewModel(application) {

    val calendars = calendarRepo.calendars
    val events = calendarRepo.events

    /**
     * Creates a calendar
     *
     * @param owner         The owner of this calendar
     * @param name          The name of this calendar
     * @param description   A description of this calendar
     * @param colour        The colour of this calendar
     * @param notifications The set of notification rules to apply to this calendar
     *
     * @return [CalendarModel] instance of the newly created calendar
     */
    suspend fun createCalendar(
        owner: UserModel,
        name: String,
        description: String,
        colour: Color,
        notifications: List<Duration> = emptyList()
    ) = calendarRepo.createCalendar(owner, name, description, colour, notifications).also {
        Notification.Channel.Calendar(it).create(applicationContext)
    }
    /**
     * Creates a event with a list of tags
     *
     * @param calendar    The calendar to add the event to
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
     * @return [EventModel] of the newly created event
     */
    suspend fun createEvent(
        calendar: CalendarModel,
        name: String,
        description: String,

        start: ZonedDateTime,
        duration: Duration,
        allDay: Boolean,
        endTimeZone: TimeZone,

        reminders: List<Duration>,

        colour: Color?,
        category: String,
        location: String,
        tags: List<String>,
        parentID: UUID?,
    ) = calendarRepo.createEvent(
        calendar,
        name, description,
        allDay, start, duration, endTimeZone,
        reminders,
        colour, category, location, tags,
        parentID
    ).also { workManager.startOneOffScheduling(it.event) }
    /**
     * Creates a event without a list of tags
     *
     * @param calendar    The calendar to add the event to
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
     * @return [EventModel] of the newly created event
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

        colour: Color?,
        category: String,
        location: String,
        parentID: UUID?,
    ) = calendarRepo.createEvent(
        calendar,
        name, description,
        allDay, start, duration, endTimeZone,
        reminders,
        colour, category, location,
        parentID
    )

    /**
     * Toggles the visibility of a calendar
     * @param calendar The calendar to toggle the visibility of
     * @return Instance of the calendar model with the visibility toggled
     */
    suspend fun toggleVisibility(calendar: CalendarModel) = calendar
            .copy(visible = !calendar.visible)
            .also { update(it) }

    /**
     * Retrieves a calendar given an ID
     * @param id The ID to be queried
     * @return The calendar (if it was found)
     */
    fun fromCalendarID(id: UUID) = calendarRepo.getCalendarFrom(id)
    /**
     * Retrieves an event given an ID
     * @param id The ID to be queried
     * @return The event (if it was found)
     */
    fun fromEventID(id: UUID) = calendarRepo.getEventFrom(id)
    /**
     * Retrieves an event given its ID and its calendar
     * @param id The ID of the event to be queried
     * @return The event and calendar (if it was found)
     */
    fun fromEventIDWithCalendar(id: UUID) = calendarRepo.getEventWithCalendar(id)
    /**
     * Retrieves an event given an ID and its children
     * @param id The ID of the event to be queried
     * @return The event and its children (if it was found)
     */
    fun fromEventIDWithChildren(id: UUID) = calendarRepo.getEventWithChildren(id)
    /**
     * Retrieves an event and its tags given an ID
     * @param id The ID of the event to be queried
     * @return The event and its tags (if it was found)
     */
    fun fromEventIDWithTags(id: UUID) = calendarRepo.getEventWithTags(id)
    /**
     * Retrieves events with a given event
     * @param id ID of the tag
     * @return List of events associated with the tag
     */
    fun fromEventTag(id: UUID) = calendarRepo.getTagFrom(id).flatMapLatest {
        it?.let(calendarRepo::getEventFrom) ?: flowOf(null)
    }
    /**
     * Retrieves a list of events whose name contains the given value
     * @param name The name to be queried
     * @return List of events whose names matched with [name]
     */
    fun fromEventName(name: String) = calendarRepo.getEventFrom(name) { anyEnd = true }
    /**
     * Retrieves a tag given its ID
     * @param id The ID of the tag
     * @return The tag with the given [id] (if it was found)
     */
    fun fromTagID(id: UUID) = calendarRepo.getTagFrom(id)
    /**
     * Retrieves a list of tags whose contents match the query
     * @param content The content value to be queried
     * @return List of tags with whose content matched with [content]
     */
    fun fromTagContent(content: String) = calendarRepo.getTagFrom(content) { anyEnd = true }


    /**
     * Retrieves the notifications associated with a [calendar]
     */
    fun notificationsOf(calendar: CalendarModel?) = flow {
        if(calendar == null) emit(emptyList())
        else emitAll(
            calendarRepo.getNotificationRulesFor(calendar)
                .filterNotNull()
                .map { it.notificationRules }
        )
    }

    /**
     * Retrieves the notifications associated with a [event]
     * Applying the calendar and override rules to the event
     */
    fun notificationsOf(event: EventModel?) = flow {
        if(event == null) emit(emptyList())
        else emitAll(calendarRepo.determineNotificationsOf(event))
    }

    /**
     * Updates the list of tags associated with an event
     * @param event The event to be updated
     * @param tags  The new list of tags
     */
    suspend fun updateTags(event: EventModel, tags: List<String>) =
        calendarRepo.updateAssociatedTags(event, tags)

    /**
     * Updates the list of notification rules associated with an event
     * @param event     The event to be updated
     * @param reminders The new list of reminders
     */
    suspend fun updateNotificationRules(event: EventModel, reminders: List<Duration>) =
        calendarRepo.updateAssociatedReminders(event, reminders).also {
            workManager.startOneOffScheduling(event)
        }
    /**
     * Updates the list of notification rules associated with an event
     * @param calendar  The calendar to be updated
     * @param reminders The new list of reminders
     */
    suspend fun updateNotificationRules(calendar: CalendarModel, reminders: List<Duration>) =
        calendarRepo.updateAssociatedReminders(calendar, reminders).also {
            workManager.startOneOffScheduling(calendar)
        }

    /**
     * Updates a calendar
     * @param calendar Calendar model which has been modified
     */
    suspend fun update(calendar: CalendarModel) = calendarRepo.update(calendar)
    /**
     * Updates a event
     * @param event Event model which has been modified
     */
    suspend fun update(event: EventModel) = calendarRepo.update(event).also {
        workManager.startOneOffScheduling(event)
    }

    /**
     * Deletes a calendar from the database
     * @param calendar Calendar to be removed
     */
    suspend fun delete(calendar: CalendarModel) = calendarRepo.delete(calendar).also {
        workManager.startOneOffScheduling(calendar)
        Notification.Channel.Calendar(calendar).delete(applicationContext)
    }
    /**
     * Deletes a event from the database
     * @param event Event to be removed
     */
    suspend fun delete(event: EventModel) = calendarRepo.delete(event).also {
        workManager.startOneOffScheduling(event)
    }
    /**
     * Deletes tag(s) from the database
     * @param tags Tags to be removed
     */
    suspend fun delete(vararg tags: EventTagModel) = calendarRepo.delete(tags.toList())

    /**
     * Retrieve a list of all events which occur on a given [date]
     * @param date The date to check
     * @return A flow containing a list of events which occur on the given date (and their
     *         associated calendars)
     */
    fun eventsOn(date: LocalDate) = eventsIn(date, date)
    /**
     * Retrieve a list of all events which occur on a given range of dates
     * @param start The first date of the range (inclusive)
     * @param end   The last date of the range (inclusive)
     * @return A flow containing a list of events which occur in the given range (and their
     *         associated calendars)
     */
    fun eventsIn(start: LocalDate, end: LocalDate) = events.map {
        // TODO: Efficient check against events in SQLite table
        it.filter { (_, event) ->
            // Event needs to start before the end date or during the end date
            // Event needs to end after the start date or during the start date
            event.start.toLocalDate().run { isEqual(end) || isBefore(end) } &&
                    event.end.toLocalDate().run { isEqual(start) || isAfter(start) }
        }
    }

}
