package uk.co.sksulai.multitasker.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.launch
import uk.co.sksulai.multitasker.util.Datastores.appStatePref

object DatastoreNames {
    const val appState = "state"
}
object DatastoreKeys {
    object AppState {
        val OnBoarded   = booleanPreferencesKey("been_onboarded")
        val CurrentUser = stringPreferencesKey("current_user")
    }
}
object Datastores {
    val Context.appStatePref by preferencesDataStore(name = DatastoreNames.appState)
}

@Composable fun getDatastore(name: String): DataStore<Preferences> {
    val context = LocalContext.current
    return when(name) {
        DatastoreNames.appState -> context.appStatePref
        else -> throw Exception("Not a valid datastore name")
    }
}

@Composable fun UseDatastore(
    name: String,
    vararg keys: Any?,
    block: suspend DataStore<Preferences>.() -> Unit) {
    val dataStore = getDatastore(name)
    LaunchedEffect(*keys){ dataStore.block() }
}
