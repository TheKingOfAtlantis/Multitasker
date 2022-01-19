package uk.co.sksulai.multitasker.db.datasource

import java.util.*
import kotlinx.coroutines.flow.Flow
import uk.co.sksulai.multitasker.db.model.CalendarModel
import uk.co.sksulai.multitasker.db.model.EventModel

interface EventDataSource {
    fun insert(event: EventModel)
    fun update(event: EventModel)
    fun delete(event: EventModel)
    fun deleteFrom(calendarID: UUID)

    fun getAll(): Flow<Map<CalendarModel, List<EventModel>>>

    fun fromID(id: UUID): Flow<EventModel?>
    fun fromCalendar(calendarID: UUID): Flow<List<EventModel>>
    fun fromName(name: String): Flow<List<EventModel>>
}