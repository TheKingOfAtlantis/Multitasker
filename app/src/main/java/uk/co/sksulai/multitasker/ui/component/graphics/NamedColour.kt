package uk.co.sksulai.multitasker.ui.component.graphics

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

import androidx.annotation.StringRes
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource

import uk.co.sksulai.multitasker.R

/**
 * Used to provide a colour with a name
 * @param name   The name of the colour
 * @param colour The value of the colour
 */
@Parcelize
@Immutable data class NamedColour(
    val name: String,
    val colour: Color
) : Parcelable
/**
 * Used to provide a colour with a name
 * @param name   The string resource containing the name of the colour
 * @param colour The value of the colour
 */
@ReadOnlyComposable
@Composable fun NamedColour(
    @StringRes name: Int,
    colour: Color
) = NamedColour(stringResource(name), colour)

/**
 * List of default colours that the user can pick from
 * TODO: Expand the selection of colours
 */
val DefaultColours
    @ReadOnlyComposable
    @Composable get() = listOf(
        NamedColour(R.string.colour_blue,    Color.Blue),
        NamedColour(R.string.colour_green,   Color.Green),
        NamedColour(R.string.colour_red,     Color.Red),
        NamedColour(R.string.colour_magenta, Color.Magenta),
        NamedColour(R.string.colour_cyan,    Color.Cyan),
        NamedColour(R.string.colour_yellow,  Color.Yellow),
    )

/**
 * Retrieves a relevant named colour where ever possible
 */
@Composable fun Color.asNamedColour(): NamedColour = DefaultColours.find {
    it.colour == this
} ?: NamedColour(R.string.colour_custom, colour = this)
