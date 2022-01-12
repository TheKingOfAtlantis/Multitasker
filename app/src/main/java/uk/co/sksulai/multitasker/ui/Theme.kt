package uk.co.sksulai.multitasker.ui

import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.foundation.*
import androidx.compose.ui.graphics.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

import uk.co.sksulai.multitasker.db.AppSettings

enum class ThemeState {
    System,
    Light,
    Dark
}

object MultitaskerPalette {
    val Primary     = Color(0xff8636b2)
    val LightAccent = Color(0xffb784a9)
    val DarkAccent  = Color(0xff647098)
    val DarkShades  = Color(0xff1e1e34)

    val Info        = Color(0xff20213a)
    val Success     = Color(0xff5d8b6d)
    val Warning     = Color(0xffdb7b35)
    val Danger      = Color(0xfff44336)
}

val Colors.Info: Color    get() = MultitaskerPalette.Info
val Colors.Success: Color get() = MultitaskerPalette.Success
val Colors.Warning: Color get() = MultitaskerPalette.Warning

object MultitaskerTheme {
    val darkTheme = darkColors(
        primary        = MultitaskerPalette.Primary,
        primaryVariant = MultitaskerPalette.DarkAccent,
        secondary      = MultitaskerPalette.LightAccent,
        error          = MultitaskerPalette.Danger
    )
    val lightTheme = lightColors(
        primary        = MultitaskerPalette.Primary,
        primaryVariant = MultitaskerPalette.DarkAccent,
        secondary      = MultitaskerPalette.LightAccent,
        error          = MultitaskerPalette.Danger
    )
    val shapes = Shapes(
        small  = RoundedCornerShape(50),
        medium = RoundedCornerShape(16.dp)
    )
}

@Composable fun MultitaskerTheme(content: @Composable () -> Unit) {
    val themePref = AppSettings.General.theme.let { pref -> // Get stored preference
        if(pref == ThemeState.System) // If to just follow the system then we need to detect what it currently is
            if(isSystemInDarkTheme())
                ThemeState.Dark else ThemeState.Light
        else pref // Otherwise just return the users preference
    }

    val theme = when(themePref) {
        ThemeState.Light -> MultitaskerTheme.lightTheme
        ThemeState.Dark  -> MultitaskerTheme.darkTheme
        else -> throw IllegalStateException("Invalid theme state")
    }

    MaterialTheme(
        colors  = theme,
        shapes  = MultitaskerTheme.shapes,
        content = content
    )
}
