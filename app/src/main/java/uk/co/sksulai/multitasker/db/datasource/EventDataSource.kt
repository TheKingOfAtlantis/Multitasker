package uk.co.sksulai.multitasker.db.datasource

import java.util.*
import kotlinx.coroutines.flow.Flow
import uk.co.sksulai.multitasker.db.model.*

interface EventDataSource {
    /**
     * Insert an event
     */
    fun insert(event: EventModel)
    /**
     * Updates an event
     */
    fun update(event: EventModel)
    /**
     * Deletes an event
     */
    fun delete(event: EventModel)

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
}
