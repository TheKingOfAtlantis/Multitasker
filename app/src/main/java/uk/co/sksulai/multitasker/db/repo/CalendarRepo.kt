package uk.co.sksulai.multitasker.db.repo

import java.time.*
import java.util.*

import javax.inject.Inject
import kotlinx.coroutines.*

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

import uk.co.sksulai.multitasker.db.dao.*
import uk.co.sksulai.multitasker.db.model.*
import uk.co.sksulai.multitasker.di.DispatcherIO

class CalendarRepo @Inject constructor(
    private val calendarDao: CalendarDao,
    private val eventDao: EventDao,
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher
) {
    // Creation

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
    ) = createCalendar(
        owner.userID,
        name,
        description,
        colour
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
        colour: Color
    ) = create(CalendarModel(
        calendarID  = generateID(),
        ownerID     = owner,
        name        = name,
        description = description,
        colour      = colour.toArgb(),
        visible     = true
    ))

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
     * @return [EventModel] instance of the newly created event
     */
    suspend fun createEvent(
        calendar: CalendarModel,
        name: String,
        description: String,
        start: OffsetDateTime,
        duration: Duration,
        allDay: Boolean,
        colour: Color?   = null,
        category: String = "",
        parentID: UUID?  = null,
    ) = createEvent(
        calendarId  = calendar.calendarID,
        name        = name,
        description = description,
        start       = start,
        duration    = duration,
        allDay      = allDay,
        colour      = colour?.toArgb(),
        category    = category,
        parentID    = parentID,
    )
    /**
     * Creates a event
     *
     * @param calendarId  The calendar to add the event to
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
     * @return [EventModel] instance of the newly created event
     */
    suspend fun createEvent(
        calendarId: UUID,
        name: String,
        description: String,
        start: OffsetDateTime,
        duration: Duration,
        allDay: Boolean,
        colour: Int?     = null,
        category: String = "",
        parentID: UUID?  = null,
    ) = create(EventModel(
        eventID     = generateID(),
        calendarID  = calendarId,
        parentID    = parentID,
        name        = name,
        description = description,
        colour      = colour,
        category    = category,
        allDay      = allDay,
        start       = start,
        duration    = duration,
    ))

    private suspend fun create(calendar: CalendarModel) = calendar.also { insert(it) }
    private suspend fun create(event: EventModel) = event.also { insert(event) }

    // Manipulation

    /**
     * Inserts a calendar into the database
     * @param calendar The calendar that is to be added
     */
    suspend fun insert(calendar: CalendarModel): Unit = withContext(ioDispatcher) {
        calendar.also(calendarDao::insert)
    }
    /**
     * Inserts an event into the database
     * @param event The calendar that is to be added
     */
    suspend fun insert(event: EventModel): Unit = withContext(ioDispatcher) {
        event.also(eventDao::insert)
    }

    /**
     * Updates a calendar
     * @param calendar Calendar model which has been modified
     */
    suspend fun update(calendar: CalendarModel) = withContext(ioDispatcher) { calendarDao.update(calendar) }
    /**
     * Updates an event
     * @param event Event model which has been modified
     */
    suspend fun update(event: EventModel) = withContext(ioDispatcher) { eventDao.update(event) }

    /**
     * Deletes a calendar from the database
     * @param calendar Calendar to be removed
     */
    suspend fun delete(calendar: CalendarModel) = withContext(ioDispatcher) { calendarDao.delete(calendar) }
    /**
     * Deletes an event from the database
     * @param event Event to be removed
     */
    suspend fun delete(event: EventModel) = withContext(ioDispatcher) { eventDao.delete(event) }

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
     * @param name Query with the name of the calendar
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
     * @param name The name of the event
     * @param queryParam Query parameters to apply to the search
     * @return Flow to the list of events found
     */
    fun getEventFrom(name: String, queryParam: QueryBuilder.() -> Unit = {}) =
        eventDao.fromName(SearchQuery.local(name, queryParam))
}

/**
 * Helper to flatten a Map with a list as its value to a list of keys and list values,
 * converting a list of type Map<K, List<V>> to List<Pair<K, V>>.
 *
 * _Intended for use with maps of type `Map<CalendarModel, List<EventModel>>`_
 */
fun <K, V> Map<K, List<V>>.toFlatList(): List<Pair<K, V>> = flatMap { entry ->
    entry.value.associateBy { entry.key }.toList()
}