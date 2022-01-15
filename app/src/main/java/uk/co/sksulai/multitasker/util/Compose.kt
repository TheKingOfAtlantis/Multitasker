package uk.co.sksulai.multitasker.util

import androidx.compose.runtime.*
import androidx.appcompat.app.AppCompatActivity


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
