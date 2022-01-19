package uk.co.sksulai.multitasker.db.viewmodel

import javax.inject.Inject
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.lifecycle.HiltViewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import uk.co.sksulai.multitasker.db.model.CalendarModel
import uk.co.sksulai.multitasker.db.model.End
import uk.co.sksulai.multitasker.db.model.EventModel
import uk.co.sksulai.multitasker.db.model.UserModel
import uk.co.sksulai.multitasker.db.repo.CalendarRepo
import uk.co.sksulai.multitasker.db.repo.toFlatList
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel class CalendarViewModel @Inject constructor(
    private val calendarRepo: CalendarRepo
) : ViewModel() {

    val calendars = calendarRepo.calendars
    val events = calendarRepo.events

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
        owner: UserModel,
        name: String,
        description: String,
        colour: Color
    ) = calendarRepo.createCalendar(owner, name, description, colour)

    /**
     * Creates a event
     *
     * @param calendar    The calendar to add the event to
     * @param name        The name of the event
     * @param description Description of the event
     * @param start       When the event starts
     * @param duration    How long the event lasts
     * @param allDay      Whether the event is an all-day event
     * @param colour      Optional colour to associated with the event
     * @param category    Category to associate with the event
     * @param tags        Tags associated with the event
     * @param parentID    ID of the parent event
     *
     * @return A [EventModel] instance of the newly created event
     */
    suspend fun createEvent(
        calendar: CalendarModel,
        name: String,
        description: String,

        start: OffsetDateTime,
        duration: Duration,
        allDay: Boolean,

        colour: Color?,
        category: String,
        tags: List<String>,
        parentID: UUID?,
    ) = calendarRepo.createEvent(
        calendar,
        name, description,
        start, duration, allDay,
        colour, category, tags.joinToString(separator = ";"),
        parentID
    )

    /**
     * Toggles the visibility of a calendar
     * @param calendar The calendar to toggle the visibility of
     */
    suspend fun toggleVisibility(calendar: CalendarModel) {
        calendarRepo.update(calendar.copy(Visible = !calendar.Visible))
    }

    /**
     * Retrieves a calendar given an ID
     * @param id The ID to be queried
     * @return Flow to the calendar if it was found
     */
    fun fromCalendarID(id: UUID) = calendarRepo.getCalendarFrom(id)
    /**
     * Retrieves an event given an ID
     * @param id The ID to be queried
     * @return Flow to the event found
     */
    fun fromEventID(id: UUID) = calendarRepo.getEventFrom(id)


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
    fun eventsIn(start: LocalDate, end: LocalDate) = events
        // TODO: Efficient check against events in SQLite table
        .map(Map<CalendarModel, List<EventModel>>::toFlatList)
        .map {
            it.filter { (_, event) ->
                // Event needs to start before the end date or during the end date
                // Event needs to end after the start date or during the start date
                event.Start.toLocalDate().run { isEqual(end) || isBefore(end) } &&
                event.End.toLocalDate().run { isEqual(start) || isAfter(start) }
            }
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
    suspend fun update(event: EventModel) = calendarRepo.update(event)

    /**
     * Deletes a calendar from the database
     * @param calendar Calendar to be removed
     */
    suspend fun delete(calendar: CalendarModel) = calendarRepo.delete(calendar)
    /**
     * Deletes a event from the database
     * @param event Event to be removed
     */
    suspend fun delete(event: EventModel) = calendarRepo.delete(event)
}
