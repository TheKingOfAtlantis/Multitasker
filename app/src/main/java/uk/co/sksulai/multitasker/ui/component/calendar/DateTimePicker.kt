package uk.co.sksulai.multitasker.ui.component.calendar

import java.time.*
import java.time.format.*

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

import com.google.accompanist.pager.ExperimentalPagerApi

import uk.co.sksulai.multitasker.ui.component.*
import uk.co.sksulai.multitasker.util.provideInScope
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
     * The default typography for the title of the date picker
     */
    val titleTypography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography.overline
    /**
     * The default typography for the current selection value show in header
     */
    val headerSelectionTypography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography.h4

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
@ExperimentalPagerApi
object DatePicker {

    /**
     * Represents the two modes by which the user may input a date via the DatePicker
     */
    enum class EditMode {
        /**
         * Indicates that the date picker should provide the grid of dates for the user
         * to provide their desired date
         */
        Picker,
        /**
         * Indicates that the date picker should provide a text field for the user
         * to input their desired date
         */
        Keyboard
    }

    /**
     * Individual components which can be used to construct a date picker
     */
    object Components {
        /**
         * Used to provide a header to a data picker
         *
         * @param title            The title to be shown in the header
         * @param value            The current selected date to show
         * @param currentMode      Current picker date edit mode
         * @param onChangeEditMode Called when the user toggles/changes the edit mode. Providing null
         *                         will disable the user's ability to change the edit mode
         * @param colour           The colour palette to use for the header section
         */
        @Composable fun Header(
            title: @Composable (() -> Unit)?,
            value: LocalDate,
            currentMode: EditMode,
            onChangeEditMode: ((EditMode) -> Unit)?,
            colour: DatePickerColours
        ) = Column {
            val textColour by colour.textColour(enabled = true, header = true, selection = false)

            if(title != null) CompositionLocalProvider(LocalContentColor provides textColour) {
                Box(
                    Modifier
                        .paddingFromBaseline(top = 32.dp)
                        .padding(start = 24.dp)
                        .align(Alignment.Start)
                ) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        ProvideTextStyle(DatePickerDefault.titleTypography, title)
                    }
                }

                Row(Modifier.fillMaxWidth()) {
                    Box(
                        Modifier
                            .paddingFromBaseline(top = 72.dp)
                            .padding(start = 24.dp, bottom = 16.dp)
                    ) {
                        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        Text(
                            formatter.format(value),
                            style = DatePickerDefault.headerSelectionTypography
                        )
                    }
                    if(onChangeEditMode != null) {
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            modifier = Modifier
                                .align(Alignment.Bottom)
                                .padding(24.dp)
                            ,
                            onClick = {
                                onChangeEditMode(when(currentMode) {
                                    EditMode.Picker   -> EditMode.Keyboard
                                    EditMode.Keyboard -> EditMode.Picker
                                })
                            }
                        ) {
                            Icon(Icons.Default.Edit, null)
                        }
                    }
                }
            }
        }

        /**
         * A set of controls which can be used by the user to change which page is currently
         * being shown. It also shows the month and year associated with the page being shown
         * as well as a toggle to show a dropdown menu containing a list of years and months to
         * quickly select another month/year
         *
         * This override provides the logic for switching to the next or previous page given
         * the pager associated with displaying the calendar grids
         *
         * @param value               The current page which is being shown
         * @param pagerState          The pager that controls which month/year is being shown
         * @param dropdownVisible     Whether the dropdown is current visible or not
         * @param onDropdownToggled   Called when the user toggles the state of the dropdown
         */
        @Composable fun Controls(
            value: YearMonth,
            pagerState: InfinitePagerState,
            dropdownVisible: Boolean,
            onDropdownToggled: (Boolean) -> Unit
        ) {
            val scope = rememberCoroutineScope()
            Controls(
                value,
                dropdownVisible, onDropdownToggled,
                provideInScope(scope) { pagerState.animateScrollToNextPage() },
                provideInScope(scope) { pagerState.animateScrollToPreviousPage() }
            )
        }
        /**
         * A set of controls which can be used by the user to change which page is currently
         * being shown. It also shows the month and year associated with the page being shown
         * as well as a toggle to show a dropdown menu containing a list of years and months to
         * quickly select another month/year
         *
         * @param value               The current page which is being shown
         * @param dropdownVisible     Whether the dropdown is current visible or not
         * @param onDropdownToggled   Called when the user toggles the state of the dropdown
         * @param onNextPageRequested Called when the user has requested the next calendar month
         * @param onPrevPageRequested Called when the user has requested the previous calendar month
         */
        @Composable fun Controls(
            value: YearMonth,
            dropdownVisible: Boolean,
            onDropdownToggled: (Boolean) -> Unit,
            onNextPageRequested: () -> Unit,
            onPrevPageRequested: () -> Unit,
        ) {
            Box(
                Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth()
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    onClick  = { onPrevPageRequested() },
                    content  = { Icon(Icons.Default.KeyboardArrowLeft, null) }
                )

                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(24.dp))
                    Text(value.format(DateTimeFormatter.ofPattern("MMMM yyyy")))
                    IconToggleButton(
                        modifier = Modifier.padding(start = 4.dp),
                        checked  = dropdownVisible,
                        onCheckedChange = { onDropdownToggled(!dropdownVisible) }
                    ) {
                        if(dropdownVisible)
                            Icon(Icons.Default.KeyboardArrowUp, null)
                        else Icon(Icons.Default.KeyboardArrowDown, null)
                    }
                }

                IconButton(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick  = { onNextPageRequested() },
                    content  = { Icon(Icons.Default.KeyboardArrowRight, null) }
                )
            }
        }
    }
}
