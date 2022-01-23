package uk.co.sksulai.multitasker.db.datasource

import java.util.*
import kotlinx.coroutines.flow.Flow
import uk.co.sksulai.multitasker.db.model.*

interface CalendarDataSource {
    /**
     * Insert an calendar
     */
    fun insert(calendar: CalendarModel)
    /**
     * Updates an calendar
     */
    fun update(calendar: CalendarModel)
    /**
     * Deletes an calendar
     */
    fun delete(calendar: CalendarModel)

    /**
     * Retrieves a list of all the [CalendarModel]s
     */
    fun getAll(): Flow<List<CalendarModel>>

    /**
     * Retrieves a [CalendarModel] given its [id]
     * @param id ID of the calendar
     */
    fun fromID(id: UUID): Flow<CalendarModel?>
    /**
     * Retrieves a list of [CalendarModel]s where [CalendarModel.name] matches the query
     * @param name The name query
     */
    fun fromName(name: String): Flow<List<CalendarModel>>
}

