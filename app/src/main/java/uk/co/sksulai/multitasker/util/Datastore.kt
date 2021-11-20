package uk.co.sksulai.multitasker.util

import android.content.Context
import androidx.compose.runtime.Composable

import androidx.compose.ui.platform.LocalContext

import androidx.datastore.dataStore
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore

sealed class DatastoreLocator<T>(
    val name: String
) {
    abstract val Context.datastore: DataStore<T>
    sealed class Preference(name: String) : DatastoreLocator<Preferences>(name) {
        override val Context.datastore by preferencesDataStore(name)
    }
    sealed class Typed<T>(
        name: String,
        serializer: Serializer<T>
    ) : DatastoreLocator<T>(name) {
        override val Context.datastore by dataStore(name, serializer)
    }
    fun retrieve(context: Context): DataStore<T> = context.datastore
    @Composable fun retrieve(): DataStore<T> = retrieve(LocalContext.current)
}
object DatastoreLocators {
    object AppState : DatastoreLocator.Preference("state") {
        val OnBoarded   = booleanPreferencesKey("been_onboarded")
        val CurrentUser = stringPreferencesKey("current_user")
    }
}
