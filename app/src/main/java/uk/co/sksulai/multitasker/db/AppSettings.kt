package uk.co.sksulai.multitasker.db

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

import uk.co.sksulai.multitasker.ui.ThemeState

object AppSettings {
    val pref : SharedPreferences
        @Composable get() = LocalContext.current.getSharedPreferences("pref", Context.MODE_PRIVATE)

    object General {
        @Composable fun setTheme(value: ThemeState) = pref.edit { putString("theme", value.toString()) }
        val theme: ThemeState
            @Composable get() = pref.getString("theme", ThemeState.System.toString())!!.let {
            ThemeState.valueOf(it)
        }
    }
}
