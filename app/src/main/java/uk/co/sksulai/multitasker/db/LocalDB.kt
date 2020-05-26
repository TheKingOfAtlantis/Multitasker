package uk.co.sksulai.multitasker.db

import android.content.Context
import androidx.room.*


inline fun <reified DB : RoomDatabase> databaseBuilder(context: Context, name: String)
        = Room.databaseBuilder(context, DB::class.java, name)
fun LocalDB.Companion.createDatabase(context: Context)
        = databaseBuilder<LocalDB>(context, DBName).build()

@Database(version = 1, entities = [
]) abstract class LocalDB : RoomDatabase() {
    companion object { const val DBName = "Multitasker.db" }
}
