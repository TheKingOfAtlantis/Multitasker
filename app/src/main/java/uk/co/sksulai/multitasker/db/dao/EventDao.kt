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

    @Query("Select * From Event") override fun getAll(): Flow<List<EventModel>>
    @Query("Select * From Event")
    @Transaction override fun getAllWithCalendar(): Flow<List<EventWithCalendar>>

    @Query("Select * From Event where eventID == :id")
    override fun fromID(id: UUID): Flow<EventModel?>
    @Query("Select * From Event Where calendarID == :id")
    override fun fromCalendar(id: UUID): Flow<List<EventModel>>
    @Query("Select * From Event where name == :name")
    override fun fromName(name: String): Flow<List<EventModel>>

    @Query("Select * From Event where eventID == :id")
    @Transaction override fun withChildren(id: UUID): Flow<EventWithChildren?>
    @Query("Select * From Event where eventID == :id")
    @Transaction override fun withCalendar(id: UUID): Flow<EventWithCalendar?>
}
