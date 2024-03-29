package uk.co.sksulai.multitasker.db

import android.content.Context
import androidx.room.*

import uk.co.sksulai.multitasker.db.converter.*
import uk.co.sksulai.multitasker.db.model.*
import uk.co.sksulai.multitasker.db.dao.*

inline fun <reified DB : RoomDatabase> databaseBuilder(context: Context, name: String, inMemory: Boolean = false)
        = if(!inMemory) Room.databaseBuilder(context, DB::class.java, name)
          else Room.inMemoryDatabaseBuilder(context, DB::class.java)
fun LocalDB.Companion.createDatabase(context: Context, inMemory: Boolean = false)
        = databaseBuilder<LocalDB>(context, DBName, inMemory)
            .fallbackToDestructiveMigration()
            .build()

@TypeConverters(
    ZonedDateTimeConverter::class,
    OffsetDateTimeConverter::class,
    LocalDateTimeConverter::class,
    LocalTimeConverter::class,
    OffsetTimeConverter::class,
    DateConverter::class,
    TimeZoneConverter::class,
    DurationConverter::class,
    InstantConverter::class,
    UriConverter::class,
    UUIDConverter::class
) @Database(version = 6, entities = [
    UserModel::class,

    CalendarModel::class,
    EventModel::class,

    EventTagModel::class,
    EventTagJunction::class,

    NotificationRuleModel::class,
    CalendarNotificationJunction::class,
    EventNotificationJunction::class,

    EventNotificationScheduleModel::class
], autoMigrations = [
    AutoMigration(from = 5, to = 6)
]) abstract class LocalDB : RoomDatabase() {
    companion object { const val DBName = "Multitasker.db" }

    abstract val userDao: UserDao

    abstract val calendarDao: CalendarDao
    abstract val eventDao: EventDao
    abstract val tagDao: TagDao
    abstract val notificationRuleDao: NotificationRuleDao

    abstract val eventNotificationDao: EventNotificationScheduleDao
}
