package uk.co.sksulai.multitasker.db

import android.content.Context
import androidx.room.*

import uk.co.sksulai.multitasker.db.converter.*
import uk.co.sksulai.multitasker.db.model.*
import uk.co.sksulai.multitasker.db.dao.*

inline fun <reified DB : RoomDatabase> databaseBuilder(context: Context, name: String)
        = Room.databaseBuilder(context, DB::class.java, name)
fun LocalDB.Companion.createDatabase(context: Context)
        = databaseBuilder<LocalDB>(context, DBName)
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
    UriConverter::class
) @Database(version = 2, entities = [
    UserModel::class
]) abstract class LocalDB : RoomDatabase() {
    companion object { const val DBName = "Multitasker.db" }

    abstract fun getUserDao(): UserDao
}
