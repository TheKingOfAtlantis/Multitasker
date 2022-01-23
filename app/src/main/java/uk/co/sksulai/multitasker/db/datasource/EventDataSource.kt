package uk.co.sksulai.multitasker.db.datasource

import java.util.*
import kotlinx.coroutines.flow.Flow
import uk.co.sksulai.multitasker.db.model.*

interface EventDataSource {
    fun insert(event: EventModel)
    fun update(event: EventModel)
    fun delete(event: EventModel)

    fun getAll(): Flow<List<EventModel>>
    fun getAllWithCalendar(): Flow<List<EventWithCalendar>>

    fun fromID(id: UUID): Flow<EventModel?>
    fun fromCalendar(id: UUID): Flow<List<EventModel>>
    fun fromName(name: String): Flow<List<EventModel>>
}
