package uk.co.sksulai.multitasker.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

import androidx.compose.Composable
import androidx.ui.core.ContextAmbient

@Composable object MultitaskerOptions {
    @Composable val pref: SharedPreferences; get() = ContextAmbient.current.getSharedPreferences("pref", Context.MODE_PRIVATE)

    @Composable object General {
        @Composable fun setTheme(value: ThemeState) = pref.edit { putString("theme", value.toString()) }
        @Composable val theme: ThemeState get() = pref.getString("theme", ThemeState.System.toString())!!.let {
            ThemeState.valueOf(it)
        }
    }
}
