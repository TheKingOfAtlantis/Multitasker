package uk.co.sksulai.multitasker.db.datasource

import java.util.*
import kotlinx.coroutines.flow.Flow
import uk.co.sksulai.multitasker.db.model.CalendarModel

interface CalendarDataSource {
    fun insert(calendar: CalendarModel)
    fun update(calendar: CalendarModel)
    fun delete(calendar: CalendarModel)

    fun getAll(): Flow<List<CalendarModel>>

    fun fromID(id: UUID): Flow<CalendarModel?>
    fun fromName(name: String): Flow<List<CalendarModel>>
}

