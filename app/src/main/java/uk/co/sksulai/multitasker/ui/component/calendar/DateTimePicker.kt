package uk.co.sksulai.multitasker.ui.component.calendar

import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.ui.graphics.Color

/**
 * Stores the various colour used by a date picker
 *
 * @param backgroundColor     The default background colour to use
 * @param headerColour        The background colour to use in the header section
 * @param textColour          The default text colour
 * @param disableTextColor    The colour to use for text which is disabled (i.e. dates which cannot be selected)
 * @param headerTextColour    The colour to used for text in the header section
 * @param selectionTextColour The colour to use for text which has been selected
 * @param selectionColour     The colour to use for the date selection indicator
 * @param todayColour         The colour to use for the today indicator
 * @param rangeColour         The colour to use for the middle portion of the range
 */
@Immutable class DatePickerColours(
    private val backgroundColor: Color,

    private val textColour: Color,
    private val disableTextColor: Color,

    private val headerColour: Color,
    private val headerTextColour: Color,
    private val selectionTextColour: Color,

    val selectionColour: Color,
    val todayColour: Color,
    val rangeColour: Color,
) {
    /**
     * Represents the colour to be used by text in a date picker
     *
     * @param enabled   Whether the text represents an enabled or disabled value
     * @param header    Whether the text is in the header section
     * @param selection Whether the text represents a selected value
     *
     * @return A state object containing the colour for the text
     */
    @Composable fun textColour(
        enabled: Boolean,
        header: Boolean,
        selection: Boolean,
    ) : State<Color> = rememberUpdatedState(when {
        header    -> headerTextColour     // Header colour is always the same regardless of the other parameters
        !enabled  -> disableTextColor     // This should override the selection colour if disabled
        selection -> selectionTextColour  // Then we set the selection text colour
        else      -> textColour           // Otherwise we fallback to the default text colour
    })

    /**
     * Represents the background colour used in a date picker
     * @param header Whether to use the header background colour
     * @return A state object containing the colour for the background
     */
    @Composable fun backgroundColour(header: Boolean): State<Color> = rememberUpdatedState(when{
        header -> headerColour
        else   -> backgroundColor
    })

}

object DatePickerDefault {
    const val rangeAlpha: Float  = .12f
    /**
     * Retrieves the colours used by the date picker
     *
     * @param backgroundColor     The default background colour to use
     * @param headerColour        The background colour to use in the header section
     * @param textColour          The default text colour
     * @param disableTextColor    The colour to use for text which is disabled (i.e. dates which cannot be selected)
     * @param headerTextColour    The colour to used for text in the header section
     * @param selectionTextColour The colour to use for text which has been selected
     * @param selectionColour     The colour to use for the date selection indicator
     * @param todayColour         The colour to use for the today indicator
     * @param rangeColour         The colour to use for the middle portion of the range
     */
    @Composable fun colours(
        backgroundColor: Color     = MaterialTheme.colors.background,
        textColour: Color          = MaterialTheme.colors.contentColorFor(backgroundColor),
        disableTextColor: Color    = textColour.copy(alpha = ContentAlpha.disabled),
        headerColour: Color        = MaterialTheme.colors.primarySurface,
        headerTextColour: Color    = MaterialTheme.colors.contentColorFor(headerColour),
        todayColour: Color         = MaterialTheme.colors.contentColorFor(backgroundColor),
        selectionColour: Color     = MaterialTheme.colors.primary,
        selectionTextColour: Color = MaterialTheme.colors.contentColorFor(selectionColour),
        rangeColour: Color         = selectionColour.copy(rangeAlpha)
    ) = DatePickerColours(
        backgroundColor     = backgroundColor,
        headerColour        = headerColour,
        textColour          = textColour,
        disableTextColor    = disableTextColor,
        headerTextColour    = headerTextColour,
        selectionTextColour = selectionTextColour,
        selectionColour     = selectionColour,
        todayColour         = todayColour,
        rangeColour         = rangeColour,
    )
}
