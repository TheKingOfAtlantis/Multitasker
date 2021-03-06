package uk.co.sksulai.multitasker.ui

import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.foundation.*
import androidx.compose.ui.graphics.*

import uk.co.sksulai.multitasker.db.AppSettings

enum class ThemeState {
    System,
    Light,
    Dark
}

object MultitaskerColour {
    object Palette {
        val Primary     = Color(0xff8636b2)
        val LightAccent = Color(0xffb784a9)
        val DarkAccent  = Color(0xff647098)
        val DarkShades  = Color(0xff1e1e34)

        val Info     = Color(0xff20213a)
        val Success  = Color(0x5d8b6d)
        val Warning  = Color(0xdb7b35)
        val Danger   = Color(0xf44336)
    }
}

val Colors.Info : Color    get() = MultitaskerColour.Palette.Info
val Colors.Success : Color get() = MultitaskerColour.Palette.Success
val Colors.Warning : Color get() = MultitaskerColour.Palette.Warning

object MultitaskerTheme {
    private val darkTheme = darkColors(
        primary        = MultitaskerColour.Palette.Primary,
        primaryVariant = MultitaskerColour.Palette.DarkAccent,
        secondary      = MultitaskerColour.Palette.LightAccent,
        error          = MultitaskerColour.Palette.Danger
    )
    private val lightTheme = lightColors(
        primary        = MultitaskerColour.Palette.Primary,
        primaryVariant = MultitaskerColour.Palette.DarkAccent,
        error          = MultitaskerColour.Palette.Danger
    )

    val currentTheme: Colors
        @Composable get() = when(AppSettings.General.theme.let { pref ->
            if(pref == ThemeState.System)
                if(isSystemInDarkTheme())
                    ThemeState.Dark else ThemeState.Light
            else pref
        }) {
            ThemeState.Light -> lightTheme
            ThemeState.Dark  -> darkTheme
            else -> throw IllegalStateException("Invalid theme state")
        }
}
