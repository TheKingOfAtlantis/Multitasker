package uk.co.sksulai.multitasker.ui.component.calendar

import java.util.*
import java.time.*
import java.time.format.*
import java.time.temporal.WeekFields
import java.text.NumberFormat

import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

import com.google.accompanist.pager.ExperimentalPagerApi

import uk.co.sksulai.multitasker.util.*
import uk.co.sksulai.multitasker.ui.component.*
import uk.co.sksulai.multitasker.util.provideInScope

fun LocalDate.toYearMonth(): YearMonth = YearMonth.of(year, month)

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
    val CellSize: Dp            = 40.dp
    val GridHeight: Dp          = CellSize * 6f
    val SelectionRadius: Dp     = 36.dp/2
    val TodayWidth: Dp          = 2.dp
    const val RangeAlpha: Float = .12f

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
        rangeColour: Color         = selectionColour.copy(RangeAlpha)
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

@ExperimentalFoundationApi
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

        /**
         * A sticky header that is placed above the calendar grid to show the relevant
         * day of the week for each column (which does not move when changing pages)
         *
         * @param modifier    Modifier to be applied to the header
         * @param size        Size of the cells containing each day of week label
         * @param startOfWeek Which day of the week is to be shown as the first
         * @param colour      Date picker colours which are to be used
         */
        @Composable fun DayOfWeekHeader(
            modifier: Modifier = Modifier,
            size: Dp = DatePickerDefault.CellSize,
            startOfWeek: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek,
            colour: DatePickerColours = DatePickerDefault.colours()
        ) {
            val startOfWeekOffset = remember(startOfWeek) { startOfWeek.value - DayOfWeek.MONDAY.value }
            LazyVerticalGrid(
                modifier = modifier,
                cells = GridCells.Fixed(7)
            ) {
                items(DayOfWeek.values().rotate(startOfWeekOffset)) {
                    val textColours by colour.textColour(enabled = true, selection = false, header = false)

                    Box(
                        Modifier.size(size),
                        contentAlignment = Alignment.Center
                    ) {
                        CompositionLocalProvider(
                            LocalContentColor provides textColours,
                            LocalContentAlpha provides textColours.alpha
                        ) {
                            Text(
                                it.getDisplayName(TextStyle.NARROW_STANDALONE, Locale.getDefault())
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Contains the Calendar date grids
     */
    object Grid {
        /**
         * Layout for the contents of an individual cell in a calendar grid
         *
         * @param modifier Modifier to apply to this layout
         * @param size     The size of the cell
         * @param onClick  Callback to handle the user pressing a cell
         * @param content  The contents of the cell
         */
        @Composable private fun Cell(
            modifier: Modifier = Modifier,
            size: Dp = DatePickerDefault.CellSize,
            onClick: (() -> Unit)? = null,
            content: @Composable () -> Unit
        ) = Box(
            modifier
                .size(size)
                .then(onClick?.let {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(radius = DatePickerDefault.CellSize / 2),
                        onClick = onClick
                    )
                } ?: Modifier),
            contentAlignment = Alignment.Center
        ) { content() }
        /**
         * Layout for the contents of an individual cell in a calendar grid
         *
         * @param value    A string that is placed within the cell
         * @param colour   Colour to apply to the text
         * @param modifier Modifier to apply to the cell layout
         * @param size     The size of the cell
         * @param onClick  Callback to handle the user pressing on a cell
         */
        @Composable private fun Cell(
            value: String,
            colour: Color,
            modifier: Modifier = Modifier,
            size: Dp = DatePickerDefault.CellSize,
            onClick: (() -> Unit)? = null,
        ) = Cell(modifier, size, onClick) {
            CompositionLocalProvider(
                LocalContentColor provides colour,
                LocalContentAlpha provides colour.alpha
            ) { Text(value) }
        }

        /**
         * This modified applies the today indicator to a cell
         *
         * @param isToday Whether or not the cell corresponds to today (thus if an indicator should be drawn)
         * @param colour  The colour of the indicator
         * @param radius  The radius of the indicator circle
         * @param width   The stroke width to be applied to the indicator
         */
        private fun Modifier.showTodayIndicator(
            isToday: Boolean,
            colour: Color,
            radius: Dp = DatePickerDefault.SelectionRadius,
            width: Dp = DatePickerDefault.TodayWidth,
        ) = if(!isToday) this else this.drawBehind {
            drawCircle(
                colour,
                radius = radius.toPx(),
                style  = Stroke(width.toPx())
            )
        }
        /**
         * This modifier applies the selection indicator to a cell
         *
         * @param isSelection Whether or not this cell corresponds to a user's selection
         * @param colour      The colour of the selection indicator
         * @param radius      The radius of the selection indicator
         */
        private fun Modifier.showSelectionIndicator(
            isSelection: Boolean,
            colour: Color,
            radius: Dp = DatePickerDefault.SelectionRadius
        ) = if(!isSelection) this else this.drawBehind {
            drawCircle(
                colour,
                radius = radius.toPx(),
            )
        }

        /** Used to represents the different parts of a range of values */
        enum class RangePart {
            /** Used to indicate that the cell does not correspond to a value within the range */
            None,
            /** Used to indicate that the cell represents the start of the range */
            Start,
            /** Used to indicate that the cell represents some period between the start and end of the range */
            Middle,
            /** Used to indicate that the cell represents the end of the range */
            End,
        }

        /**
         * This modifier applies the relevant range indicator
         *
         * @param part     Which part of the range does this cell correspond to
         * @param colour   The colour to apply to the range indicator
         * @param height   The height of the range indicator
         * @param cellSize The size of the cell
         */
        private fun Modifier.showRangeIndicator(
            part: RangePart,
            colour: Color,
            height: Dp = DatePickerDefault.SelectionRadius,
            cellSize: Dp = DatePickerDefault.CellSize,
        ) = if(part == RangePart.None) this else this.drawBehind {
            val offset = (cellSize - height)/2
            when(part) {
                // TODO: Handle LayoutDirection => LTR vs RTL
                RangePart.Start -> drawRect(
                    colour,
                    Offset(size.width/2, offset.toPx()),
                    Size(
                        width  = size.width/2,
                        height = size.height - offset.toPx()
                    ),
                    colour.alpha
                )
                RangePart.End -> drawRect(
                    colour,
                    Offset(0f, offset.toPx()),
                    Size(
                        width  = size.width/2,
                        height = size.height - offset.toPx()
                    ),
                    colour.alpha
                )
                RangePart.Middle -> drawRect(
                    colour,
                    Offset(0f, offset.toPx()),
                    Size(
                        width  = size.width,
                        height = size.height - offset.toPx()
                    ),
                    colour.alpha
                )
            }
        }

        /**
         * This layout takes in a month/year as the [page] value and produces a grid which
         * the user can interact with. It uses a series of parameters to query if various
         * indicators should be drawn on each date and also provides a callback to respond
         * to a user's selection
         *
         * @param page            The month/year grid to show
         * @param onValueSelected Passes the users selection
         * @param showDaysOfWeek  Whether to show the days of the week at the top
         * @param startOfWeek     Used to determine which day of the week to show as the first
         * @param isSelection     Callback used to determines if a date is the selected date
         * @param isSelectable    Callback used to determines if a date is enabled/selectable
         * @param inRange         Callback used to determines which part a range a date is. If the
         *                        date is the first date of the range then should return [RangePart.Start],
         *                        if is the last date of the range then should return [RangePart.End], if
         *                        some date in the middle then [RangePart.Middle] otherwise [RangePart.None]
         *                        if it is not part of the range.
         * @param isToday         Callback used to determines if a date corresponds to today
         */
        @Composable private fun GridPage(
            page: YearMonth,
            onValueSelected: (LocalDate) -> Unit,
            showDaysOfWeek: Boolean,
            startOfWeek: DayOfWeek,
            isSelection: (LocalDate) -> Boolean,
            isSelectable: (LocalDate) -> Boolean,
            inRange: (LocalDate) -> RangePart,
            modifier: Modifier = Modifier,
            isToday: (LocalDate) -> Boolean = { LocalDate.now() == it },
            colour: DatePickerColours = DatePickerDefault.colours()
        ) {
            val startOfWeekOffset = remember(startOfWeek) { startOfWeek.value - DayOfWeek.MONDAY.value }
            val dayOfWeekOffset   = remember(page) {
                val monthStartOffset = page.atDay(1).dayOfWeek.value - DayOfWeek.MONDAY.value

                if(monthStartOffset > startOfWeekOffset) monthStartOffset - startOfWeekOffset
                else (monthStartOffset - startOfWeekOffset + 7) % 7
            }

            LazyVerticalGrid(
                modifier = modifier
                    .height(DatePickerDefault.GridHeight),
                cells = GridCells.Fixed(7)
            ) {
                if(showDaysOfWeek) items(DayOfWeek.values().rotate(startOfWeekOffset)) {
                    val daysOfWeekColour by colour.textColour(enabled = true, selection = false, header = false)
                    Cell(
                        it.getDisplayName(TextStyle.NARROW_STANDALONE, Locale.getDefault()),
                        daysOfWeekColour
                    )
                }
                if(dayOfWeekOffset != 0) item({ GridItemSpan(dayOfWeekOffset) }) { Box(Modifier.size(1.dp)) }

                items(page.lengthOfMonth()) {
                    val day = page.atDay(it + 1)

                    val textColour by colour.textColour(
                        enabled   = isSelectable(day),
                        selection = isSelection(day),
                        header = false
                    )

                    Cell(
                        modifier = Modifier
                            .showTodayIndicator(
                                isToday = isToday(day),
                                colour = colour.todayColour
                            )
                            .showRangeIndicator(
                                part = inRange(day),
                                colour = colour.rangeColour,
                            )
                            .showSelectionIndicator(
                                isSelection = isSelection(day),
                                colour = colour.selectionColour
                            ),
                        onClick = { onValueSelected(day) }.takeIf { isSelectable(day) },
                        value   = NumberFormat.getInstance().format(day.dayOfMonth),
                        colour  = textColour
                    )
                }
            }
        }

        /**
         * Determines the value of a page given its index in the pager
         *
         * @param initial The value of the initial page
         * @param page    The page index value
         *
         * @return [YearMonth] to be given to [GridPage]
         */
        fun calculatePage(initial: LocalDate, page: Int) = initial
            .plusMonths(page.toLong())
            .withDayOfMonth(1)
            .toYearMonth()

        /**
         * Used to provide the user with the ability to select a single date. The pages are placed
         * inside of a pager to enable the user to swipe between months. It also handles the
         * selection of dates ensures the correct indicators are shown.
         *
         * @param initial         The initial local date given - used to keep track of page 0
         * @param value           The current date selection
         * @param onValueSelected Called when a new value has been selected
         * @param modifier        Modifier to apply to the pager
         * @param onPageChange    Callback which gets the target page value
         * @param isSelectable    Callback which is used to determine if a given date can be selected by the user
         * @param showDaysOfWeek  Whether the days of the week are shown as part of each page
         * @param startOfWeek     The start of the week
         * @param state           The pager state used control and observe the internal pager
         * @param colour          The colour picker colour to use
         */
        @Composable fun Single(
            initial: LocalDate,
            value: LocalDate,
            onValueSelected: (LocalDate) -> Unit,
            modifier: Modifier = Modifier,
            onPageChange: ((YearMonth) -> Unit)? = null,
            isSelectable: (LocalDate) -> Boolean = { true },
            showDaysOfWeek: Boolean = true,
            startOfWeek: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek, // TODO: User should be able to override this
            state: InfinitePagerState = rememberInfinitePagerState(),
            colour: DatePickerColours = DatePickerDefault.colours()
        ) {
            InfiniteHorizontalPager(
                modifier,
                state
            ) { page ->
                val currentPage = calculatePage(initial, page)
                GridPage(
                    currentPage,
                    onValueSelected,
                    showDaysOfWeek,
                    startOfWeek,
                    isSelectable = isSelectable,
                    isSelection = { value == it },
                    inRange = { RangePart.None },  // Only single value - so don't show ranges
                    colour = colour
                )
            }
            if(onPageChange != null) LaunchedEffect(state.targetPage) {
                onPageChange(calculatePage(initial, state.targetPage))
            }
        }
    }
}
