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
    @Query("Delete From Event where CalendarID == :calendarID")
    override fun deleteFrom(calendarID: UUID)

    @Query("Select * From Calendar Inner Join Event On Event.CalendarID == Calendar.ID")
    override fun getAll(): Flow<Map<CalendarModel, List<EventModel>>>

    @Query("Select * From Event where ID == :id")
    override fun fromID(id: UUID): Flow<EventModel?>
    @Query("Select * From Event Where CalendarID == :calendarID")
    override fun fromCalendar(calendarID: UUID): Flow<List<EventModel>>
    @Query("Select * From Event where Name == :name")
    override fun fromName(name: String): Flow<List<EventModel>>
}
