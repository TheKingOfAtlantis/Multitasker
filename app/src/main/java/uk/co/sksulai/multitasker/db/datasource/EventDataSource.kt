package uk.co.sksulai.multitasker.db.datasource

import java.util.*
import kotlinx.coroutines.flow.Flow
import uk.co.sksulai.multitasker.db.model.*

interface EventDataSource {
    /**
     * Insert an event
     */
    suspend fun insert(event: EventModel)
    /**
     * Updates an event
     */
    suspend fun update(event: EventModel)
    /**
     * Deletes an event
     */
    suspend fun delete(event: EventModel)

    /**
     * Retrieves a list of all the [EventModel]s
     */
    fun getAll(): Flow<List<EventModel>>
    /**
     * Retrieves a list of all the [EventModel]s associated with their calendar
     */
    fun getAllWithCalendar(): Flow<List<EventWithCalendar>>

    /**
     * Retrieves a [EventModel] given its [id]
     * @param id ID of the event
     */
    fun fromID(id: UUID): Flow<EventModel?>
    /**
     * Retrieves all the [EventModel] in a given [CalendarModel]
     * @param id ID of the [CalendarModel]
     */
    fun fromCalendar(id: UUID): Flow<List<EventModel>>
    /**
     * Retrieves a list of [EventModel]s where [EventModel.name] matches the query
     * @param name The name query
     */
    fun fromName(name: String): Flow<List<EventModel>>

    /**
     * Retrieves a [EventModel] with its children
     * @param id ID of the parent [EventModel]
     * @return [EventWithChildren] containing the queried parent as [EventWithChildren.parent]
     *         and [EventWithChildren.children] being a list of events where [EventModel.parentID]
     *         is equal to the parents [EventModel.eventID]
     */
    fun withChildren(id: UUID): Flow<EventWithChildren?>
    /**
     * Retrieves a [EventModel] with its associated [CalendarModel]
     * @param id ID of the event to retrieve
     * @return [EventWithChildren] containing the queried event with the [CalendarModel] where
     *         [EventModel.calendarID] is equal to [CalendarModel.calendarID]
     */
    fun withCalendar(id: UUID): Flow<EventWithCalendar?>
}
