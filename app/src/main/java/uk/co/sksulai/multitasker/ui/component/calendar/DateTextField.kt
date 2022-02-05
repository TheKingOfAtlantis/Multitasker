package uk.co.sksulai.multitasker.ui.component.calendar

import java.util.*
import java.text.*
import java.time.*
import java.time.format.*

import androidx.compose.runtime.*
import androidx.compose.foundation.text.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.ExperimentalGraphicsApi
import androidx.compose.ui.unit.ExperimentalUnitApi
import com.google.accompanist.pager.ExperimentalPagerApi

import uk.co.sksulai.multitasker.util.*

/**
 * Determines the required date format using the current locale and remembers
 * it across recompositions
 *
 * @param format The desired format (default: [DateFormat.SHORT])
 * @param locale The locale to use (default: [Locale.getDefault()])
 *
 * @return The date format pattern, e.g. yyyy-MM-dd
 */
@Composable private fun rememberDateFormatPattern(
    format: Int    = DateFormat.SHORT,
    locale: Locale = Locale.getDefault()
): String = remember {
    val dateFormat = SimpleDateFormat.getDateInstance(format, locale) as SimpleDateFormat
    dateFormat.timeZone = TimeZone.getDefault()
    var pattern = dateFormat.toPattern()

    // Fix the size of each date part
    if(pattern.replace(Regex("[^d]"), "").length == 1)
        pattern = pattern.replace(Regex("d+"), "dd")
    if(pattern.replace(Regex("[^M]"), "").length == 1)
        pattern = pattern.replace(Regex("M+"), "MM")
    if(pattern.replace(Regex("[^y]"), "").length < 4)
        pattern = pattern.replace(Regex("y+"), "yyyy")

    pattern
}

/**
 * Determines the date format hint to provide to the user
 *
 * @param pattern The date format pattern to localise
 * @param locale  The locale to use for localisation
 *
 * @return The date format hint presented in the user's language
 */
@Composable private fun rememberDateHint(
    pattern: String,
    locale: Locale = Locale.getDefault()
): String = remember(pattern) {
    // Based on getTextInputHint(...) in https://github.com/material-components/material-components-android/
    // Get the date format symbols
    val symbols          = DateFormatSymbols.getInstance(Locale.ROOT).localPatternChars
    val localisedSymbols = DateFormatSymbols.getInstance(locale).localPatternChars
    // Generate a mapping between localised and unlocalised version
    val symbolsMap = localisedSymbols
        .mapIndexed { index, c  ->  c to symbols[index] }
        .toMap()
    pattern.map {
        if(!symbolsMap.contains(it)) it // Keep symbols we cannot map
        else symbolsMap.getValue(it)    // Map to localised date symbol
    }
    .joinToString("")           // Join list together into string
    .uppercase(Locale.getDefault())     // Then make uppercase
}

/**
 * Text field which rather than taking a string takes and provides a [LocalDate]
 *
 * @param value The current date
 * @param onValueChange An optional callback to retrieve the raw string value as provided by text field
 * @param onValueComplete The value of the string parsed as a [LocalDate] object (or null if the field
 * has been left empty). If the value of the string is incomplete or incorrectly formatted then no
 * new value is provided.
 * @param onFormatError Used to report formatting errors back to the user - Where possible this
 * callback will provide user readable messages but will in rare cases fallback to the index position
 * provided by [DateTimeParseException]
 * @param modifier a [Modifier] for this text field
 * @param enabled controls the enabled state of the [OutlinedTextField]. When `false`, the text field
 * will be neither editable nor focusable, the input of the text field will not be selectable,
 * visually text field will appear in the disabled UI state
 * @param readOnly controls the editable state of the [OutlinedTextField]. When `true`, the text
 * field can not be modified, however, a user can focus it and copy text from it. Pressing on the
 * text field will instead present the user with a date picker
 * @param textStyle the style to be applied to the input text. The default [textStyle] uses the
 * [LocalTextStyle] defined by the theme
 * @param label the optional label to be displayed inside the text field container. The default
 * text style for internal [Text] is [Typography.caption] when the text field is in focus and
 * [Typography.subtitle1] when the text field is not in focus
 * @param leadingIcon the optional leading icon to be displayed at the beginning of the text field
 * container
 * @param trailingIcon the optional trailing icon to be displayed at the end of the text field
 * container
 * @param isError indicates if the text field's current value is in error. If set to true, the
 * label, bottom indicator and trailing icon by default will be displayed in error color
 * @param keyboardOptions software keyboard options that contains configuration such as
 * [KeyboardType] and [ImeAction]
 * @param keyboardActions when the input service emits an IME action, the corresponding callback
 * is called. Note that this IME action may be different from what you specified in
 * [KeyboardOptions.imeAction]
 * @param interactionSource the [MutableInteractionSource] representing the stream of
 * [Interaction]s for this OutlinedTextField. You can create and pass in your own remembered
 * [MutableInteractionSource] if you want to observe [Interaction]s and customize the
 * appearance / behavior of this OutlinedTextField in different [Interaction]s.
 * @param shape the shape of the text field's border
 * @param colors [TextFieldColors] that will be used to resolve color of the text and content
 * (including label, placeholder, leading and trailing icons, border) for this text field in
 * different states. See [TextFieldDefaults.outlinedTextFieldColors]
 */
@ExperimentalFoundationApi
@ExperimentalPagerApi
@Composable fun DateTextField(
    value: LocalDate?,
    onValueComplete: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    onValueChange: ((String) -> Unit)? = null,
    onFormatError: ((String) -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.small,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors()
) = Column(modifier) {
    val pattern   = rememberDateFormatPattern()
    val hint      = rememberDateHint(pattern = pattern)
    val formatter = remember(pattern) { DateTimeFormatter.ofPattern(pattern) }

    var valueString by rememberSaveableMutableState(value?.let(formatter::format) ?: "")

    OutlinedTextField(
        modifier      = modifier,
        label         = label,
        value         = valueString,
        placeholder   = { Text(hint) },
        onValueChange = {
            // If the next value in the pattern is not a letter (i.e. not dMy)
            // then must be deliminator thus we append it to the current string
            valueString = it + if(!pattern[it.length].isLetter()) "" else pattern[it.length]
            onValueChange?.invoke(it)
            try {
                if(it.isEmpty()) onValueComplete(null)
                else if(it.length == pattern.length) onValueComplete(LocalDate.parse(it, formatter))
                else onFormatError?.invoke("Provided value is too long, it should fit: $hint")
            } catch(e: DateTimeParseException) {
                onFormatError?.invoke(when(pattern[e.errorIndex]) {
                    //TODO: More advanced error reporting such as day-of-month range being relevant
                    //      to the month the user provided, e.g. 30/02/2022 => Should provide range 01-28
                    'd'  -> "Ensure day of month is in range 01-31"
                    'M'  -> "Ensure month of year is in range: 01-12"
                    'y'  -> "Ensure year is 4 digits long"
                    else -> "Unknown error at ${e.errorIndex + 1}"
                })
            }
        },
        singleLine        = true,
        enabled           = enabled,
        readOnly          = readOnly,
        textStyle         = textStyle,
        leadingIcon       = leadingIcon,
        trailingIcon      = trailingIcon,
        isError           = isError,
        keyboardOptions   = keyboardOptions,
        keyboardActions   = keyboardActions,
        interactionSource = interactionSource,
        shape             = shape,
        colors            = colors,
    )
    if(readOnly) {
        val isPressed by interactionSource.collectIsPressedAsState()
        var showDatePicker by rememberSaveableMutableState(false)

        if(isPressed) showDatePicker = true
        if(showDatePicker) DatePicker.Dialog.Single(
            value ?: LocalDate.now(),
            onValueComplete,
            title = label,
            onDismissRequest = { showDatePicker = false }
        )
    }
}

/**
 * Provides a pair of TextFields which used to provide the user with an input to provide date
 * and time values. Unlike the date field this only allows for values to be provided via the
 * relevant pickers.
 *
 * @param value         The current value of the date/time (as a [LocalDateTime])
 * @param onValueChange The users selection after changing either the date or time component
 * @param modifier      Modifier to applied to this layout
 * @param textStyle     The text style to apply to the date and time fields
 * @param headerLabel   An optional label to place above both text fields
 * @param dateLabel     An optional label to provide to the time [OutlinedTextField]
 * @param timeLabel     An optional label to provide to the date [OutlinedTextField]
 * @param leadingIcon   An optional icon which is provided only to the date field
 * @param trailingIcon  An optional icon which is provided only to the time field
 * @param dateInteractionSource the [MutableInteractionSource] representing the stream of
 * [Interaction]s for the date TextField. You can create and pass in your own remembered
 * [MutableInteractionSource] if you want to observe [Interaction]s and customize the
 * appearance / behavior of the OutlinedTextField in different [Interaction]s.
 * @param timeInteractionSource the [MutableInteractionSource] representing the stream of
 * [Interaction]s for the time TextField. You can create and pass in your own remembered
 * [MutableInteractionSource] if you want to observe [Interaction]s and customize the
 * appearance / behavior of this OutlinedTextField in different [Interaction]s.
 * @param shape the shape of the text field's border
 * @param colors [TextFieldColors] that will be used to resolve color of the text and content
 * (including label, placeholder, leading and trailing icons, border) for this text field in
 * different states. See [TextFieldDefaults.outlinedTextFieldColors]
 */
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalGraphicsApi
@ExperimentalPagerApi
@ExperimentalUnitApi
@Composable fun DateTimeField(
    value: LocalDateTime,
    onValueChange: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    headerLabel: @Composable (() -> Unit)? = null,
    dateLabel: @Composable (() -> Unit)? = { Text("Date") },
    timeLabel: @Composable (() -> Unit)? = { Text("Time") },
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    dateInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    timeInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.small,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors()
) = Column(modifier) {

    headerLabel?.let {
        Box(
            Modifier
                .padding(start = 8.dp)
                .paddingFromBaseline(bottom = 4.dp)
        ) {
            CompositionLocalProvider(
               LocalContentAlpha provides ContentAlpha.medium
            ) { ProvideTextStyle(MaterialTheme.typography.caption, it) }
        }
    }
    Row(Modifier.width(TextFieldDefaults.MinWidth)) {
        val datePattern = rememberDateFormatPattern()
        val dateFormat  = remember(datePattern) { DateTimeFormatter.ofPattern(datePattern) }

        val timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

        OutlinedTextField(
            modifier = Modifier.weight(.55f),
            label    = dateLabel,
            value    = dateFormat.format(value),
            onValueChange = { },
            leadingIcon   = leadingIcon,
            interactionSource = dateInteractionSource,
            shape     = shape,
            colors    = colors,
            readOnly  = true,
            textStyle = textStyle.copy(textAlign = TextAlign.Center)
        )
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            modifier = Modifier.weight(.45f),
            label    = timeLabel,
            value    = timeFormat.format(value),
            onValueChange = { },
            trailingIcon  = trailingIcon,
            interactionSource = timeInteractionSource,
            shape     = shape,
            colors    = colors,
            readOnly  = true,
            textStyle = textStyle.copy(textAlign = TextAlign.Center)
        )
    }

    var showDatePicker by rememberSaveableMutableState(false)
    var showTimePicker by rememberSaveableMutableState(false)

    val datePressed by dateInteractionSource.collectIsPressedAsState()
    val timePressed by timeInteractionSource.collectIsPressedAsState()
    if(datePressed) showDatePicker = true
    if(timePressed) showTimePicker = true

    // TODO: Add time picker
    if(showDatePicker) DatePicker.Dialog.Single(
        value = value.toLocalDate(),
        onValueSelected = {
            onValueChange(it.atTime(value.toLocalTime()))
            showDatePicker = false
        },
        onDismissRequest = { showDatePicker = false }
    )
    if(showTimePicker) TimePicker.Dialog(
        value = value.toLocalTime(),
        onValueSelected = {
            onValueChange(it.atDate(value.toLocalDate()))
            showTimePicker = false
        },
        onDismissRequest = { showTimePicker = false }
    )
}
