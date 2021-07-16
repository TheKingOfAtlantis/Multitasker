package uk.co.sksulai.multitasker.util

import androidx.compose.runtime.*

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.LocalContext

@Composable fun sharedPreferences(name: String, mode: Int = Context.MODE_PRIVATE): SharedPreferences =
    LocalContext.current.getSharedPreferences(name, mode)

val LocalActivity = staticCompositionLocalOf<AppCompatActivity> {
    error("CompositionLocal LocalActivity not present")
}

@Composable fun ProvideActivity(
    activity: AppCompatActivity,
    block: @Composable () -> Unit
) = CompositionLocalProvider(
    LocalActivity provides activity,
    content = block
)
