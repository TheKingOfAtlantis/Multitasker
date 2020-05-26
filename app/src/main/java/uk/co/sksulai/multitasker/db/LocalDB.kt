package uk.co.sksulai.multitasker.db

import android.content.Context
import androidx.room.*

import uk.co.sksulai.multitasker.db.converter.*
import uk.co.sksulai.multitasker.db.model.*

inline fun <reified DB : RoomDatabase> databaseBuilder(context: Context, name: String)
        = Room.databaseBuilder(context, DB::class.java, name)
fun LocalDB.Companion.createDatabase(context: Context)
        = databaseBuilder<LocalDB>(context, DBName).build()

@TypeConverters(
    DateConverter::class,
    TimeConverter::class,
    DateTimeConverter::class,
    UriConverter::class
) @Database(version = 1, entities = [
    UserModel::class
]) abstract class LocalDB : RoomDatabase() {
    companion object { const val DBName = "Multitasker.db" }
}
