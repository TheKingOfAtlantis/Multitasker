package uk.co.sksulai.multitasker.ui

import androidx.compose.Composable
import androidx.ui.foundation.isSystemInDarkTheme
import androidx.ui.graphics.Color
import androidx.ui.material.*

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

object MultitaskerTheme {
    private val darkTheme = darkColorPalette(
        primary        = MultitaskerColour.Palette.Primary,
        primaryVariant = MultitaskerColour.Palette.DarkAccent,
        secondary      = MultitaskerColour.Palette.LightAccent,
        error          = MultitaskerColour.Palette.Danger
    )
    private val lightTheme = lightColorPalette(
        primary        = MultitaskerColour.Palette.Primary,
        primaryVariant = MultitaskerColour.Palette.DarkAccent,
        error          = MultitaskerColour.Palette.Danger
    )

    @Composable val currentTheme: ColorPalette get() =
        when(MultitaskerOptions.General.theme.let { pref ->
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
