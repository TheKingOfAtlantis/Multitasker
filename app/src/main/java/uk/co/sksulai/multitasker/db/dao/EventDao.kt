package uk.co.sksulai.multitasker.db.dao

import java.util.*
import kotlinx.coroutines.flow.Flow

import androidx.room.*

import uk.co.sksulai.multitasker.db.model.*
import uk.co.sksulai.multitasker.db.datasource.EventDataSource

@Dao interface EventDao : EventDataSource {
    @Insert override fun insert(event: EventModel)
    @Update override fun update(event: EventModel)
    @Delete override fun delete(event: EventModel)
    @Query("Delete From Event where calendarID == :calendarID")
    override fun deleteFrom(calendarID: UUID)

    @Query("Select * From Calendar Inner Join Event On Event.calendarID == Calendar.calendarID")
    override fun getAll(): Flow<Map<CalendarModel, List<EventModel>>>

    @Query("Select * From Event where eventID == :id")
    override fun fromID(id: UUID): Flow<EventModel?>
    @Query("Select * From Event Where calendarID == :calendarID")
    override fun fromCalendar(calendarID: UUID): Flow<List<EventModel>>
    @Query("Select * From Event where name == :name")
    override fun fromName(name: String): Flow<List<EventModel>>
}
