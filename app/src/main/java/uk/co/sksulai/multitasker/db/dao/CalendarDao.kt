package uk.co.sksulai.multitasker.db.dao

import java.util.*
import kotlinx.coroutines.flow.Flow

import androidx.room.*

import uk.co.sksulai.multitasker.db.model.CalendarModel
import uk.co.sksulai.multitasker.db.datasource.CalendarDataSource

@Dao interface CalendarDao : CalendarDataSource, DatabaseService {
    @Insert override suspend fun insert(calendar: CalendarModel)
    @Update override suspend fun update(calendar: CalendarModel)
    @Delete override suspend fun delete(calendar: CalendarModel)

    @Query("Select * From Calendar")
    override fun getAll(): Flow<List<CalendarModel>>

    @Query("Select * From Calendar where calendarID == :id")
    override fun fromID(id: UUID): Flow<CalendarModel?>
    @Query("Select * From Calendar where name == :name")
    override fun fromName(name: String): Flow<List<CalendarModel>>
}
