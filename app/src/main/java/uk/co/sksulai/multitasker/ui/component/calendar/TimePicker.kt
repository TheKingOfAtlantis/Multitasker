package uk.co.sksulai.multitasker.ui.component.calendar

import kotlin.math.*
import java.time.format.DateTimeFormatter
import java.text.NumberFormat
import java.time.LocalTime
import java.time.temporal.ChronoField
import android.text.format.DateFormat
import kotlinx.coroutines.flow.collect

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*

import uk.co.sksulai.multitasker.ui.component.NumberField
import uk.co.sksulai.multitasker.util.rememberSaveableMutableState

/**
 * @param backgroundColour     The colour of the background
 * @param dialBackgroundColour The background colour of the dial
 * @param textColour           The colour of the text
 * @param dialTextColour       The colour of text on the dial
 * @param outlineColour        The colour of the AM/PM and text field outline
 * @param selectionColour      The colour of the selection indicator
 * @param dialArmColour        The colour of the arms on the dial
 * @param selectionTextColour  The colour of the text of a selection
 */
@Immutable data class TimePickerColour(
    private val backgroundColour: Color,
    private val dialBackgroundColour: Color,
    private val textColour: Color,
    private val dialTextColour: Color,
    val dialArmColour: Color,
    val outlineColour: Color,
    val selectionColour: Color,
    val selectionTextColour: Color
) {

    @Composable fun textColour(
        dial: Boolean = false,
        selection: Boolean = false
    ) = rememberUpdatedState(when {
        dial -> dialTextColour
        selection -> selectionTextColour
        else -> textColour
    })

    @Composable fun backgroundColour(
        dial: Boolean = false,
        selection: Boolean = false
    ) = rememberUpdatedState(when {
        dial -> dialBackgroundColour
        selection -> selectionColour
        else -> backgroundColour
    })
}

@Stable object TimePickerDefault {
    @Composable fun colour(
        backgroundColour: Color = MaterialTheme.colors.surface,
        dialBackgroundColour: Color = MaterialTheme.colors.secondaryVariant,
        textColour: Color = MaterialTheme.colors.contentColorFor(backgroundColour),
        dialTextColour: Color = MaterialTheme.colors.contentColorFor(dialBackgroundColour),
        outlineColour: Color = MaterialTheme.colors.onSurface.copy(ContentAlpha.disabled),
        selectionColour: Color = MaterialTheme.colors.primary,
        dialArmColour: Color = MaterialTheme.colors.secondary,
        selectionTextColour: Color = MaterialTheme.colors.contentColorFor(selectionColour)
    ) = TimePickerColour(
        backgroundColour = backgroundColour,
        dialBackgroundColour = dialBackgroundColour,
        textColour = textColour,
        dialTextColour = dialTextColour,
        outlineColour = outlineColour,
        selectionColour = selectionColour,
        dialArmColour = dialArmColour,
        selectionTextColour = selectionTextColour
    )
}

@ExperimentalUnitApi
@ExperimentalComposeUiApi
@ExperimentalGraphicsApi
object TimePicker {
    enum class EditMode { Keyboard, Picker }
    enum class DayDivision { AM, PM }

    object Components {

        /**
         * Generic based from which the hour and minute dials built
         *
         * @param value The current index position of the dial hand (in range 0..[steps])
         * @param onValueChange Called when the user moves the dial to a new position
         * @param steps    The number of steps around the dial
         * @param labels   Set of labels to be placed at specific positions around the dial
         * @param modifier Modifier to be applied to the dial
         * @param rings    Sets the number of rings to split the steps among, by default all values
         *                 are on a single ring around the outer edge, however additional rings are
         *                 added inwards with higher values being further in
         * @param interactionSource A stream of interactions which can be used to observe the user's
         *                 interactions with the dial
         */
        @Composable private fun Dial(
            value: Int,
            onValueChange: (Int) -> Unit,
            steps: Int,
            labels: Map<Int, String>,
            modifier: Modifier = Modifier,
            rings: Int = 1,
            interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
            colour: TimePickerColour
        ) {
            if(value !in 0 until steps) throw IndexOutOfBoundsException(
                "Expected position to be in the range [0, steps: ${steps}) but was: $value"
            )

            val diameter      = 256.dp
            val paddingRadius = 16.dp
            val radius        = diameter/2
            val ringWidth     = 36.dp

            val selectionColour = colour.dialArmColour
            val dialBackgroundColour by colour.backgroundColour(dial = true, selection = true)
            val dialTextColour by colour.textColour(dial = true)

            /**
             * The number of steps in each ring
             */
            val stepsPerRing = remember(steps, rings) { steps/rings }
            /**
             * Retrieves the ring track index associated with particular [position] index
             */
            fun getRing(position: Int) = (position.toFloat()/stepsPerRing).toInt()
            /**
             * Retrieves the angle around the dial associated with particular [position] index
             */
            fun getAngle(position: Int) =
                // Divide the number of steps by the number of rings to get the number of divisions
                // of the dial at each ring level
                // Then wrap the angle to 360°/2π
                ((2 * PI) * (position.toFloat()/(steps/rings)) % (2 * PI))

            Layout(
                modifier = modifier.drawBehind {
                    drawCircle(
                        dialBackgroundColour,
                        radius = radius.toPx(),
                    )
                }
                .pointerInput(Unit) {
                    forEachGesture {
                        awaitPointerEventScope {
                            fun updatePosition(position: Offset) {
                                val newPosition = position - Offset(diameter.toPx(), diameter.toPx()) / 2f
                                val newAngle    = (newPosition.run { atan2(x, -y) } + 2 * PI) % (2 * PI)

                                // Work out which track the pointer in on
                                // Then coarse value to ensure we don't try and get value from a non-existent track
                                val ring = ((radius.toPx() - newPosition.getDistance())/ringWidth.toPx()).toInt()

                                // Ignore the centre
                                if(ring > rings - 1) return

                                onValueChange(run {
                                    val ringOffset   = ring * stepsPerRing
                                    val ringPosition = (newAngle/(2 * PI) * stepsPerRing).roundToInt() % stepsPerRing

                                    ringPosition + ringOffset
                                })
                                // TODO: Handle haptic feedback
                            }

                            val down = awaitFirstDown()
                            val startInteraction = DragInteraction.Start()
                            interactionSource.tryEmit(startInteraction)
                            updatePosition(down.position)

                            while(true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.first { it.id == down.id }

                                if(change.changedToUpIgnoreConsumed()) {
                                    interactionSource.tryEmit(DragInteraction.Stop(startInteraction))
                                    break
                                }

                                updatePosition(change.position)
                                change.consumeAllChanges()
                            }
                        }
                    }
                },
                content = {
                    labels.forEach { (position, hour) ->
                        Text(
                            text = hour,
                            modifier = Modifier.layoutId("label-$position"),
                            color = dialTextColour
                        )
                    }

                    Canvas(
                        Modifier
                            .layoutId("arm")
                            .fillMaxSize()
                    ) {
                        val angle = getAngle(value)
                        val track = getRing(value)
                        // TODO: Investigate if labels are truly positioned in a circle and not a
                        //       oval that almost appears as a circle
                        // TODO: Determine the best value for the final arm adjustment
                        val radius = size.width/2 - paddingRadius.toPx() - 8.dp.toPx() - ringWidth.toPx() * track

                        val armEnd = center + Offset(
                            sin(angle)  * radius,
                            -cos(angle) * radius
                        )

                        // Clock dial arm
                        drawLine(
                            selectionColour,
                            start = center,
                            end   = armEnd,
                            strokeWidth = 2.dp.toPx()
                        )

                        val selectionRadius = 20.dp
                        // Selection indicator
                        drawCircle(
                            selectionColour,
                            radius = selectionRadius.toPx(),
                            center = armEnd
                        )
                        if(value !in labels.keys)
                            drawCircle(
                                Color.hsl(1f, 1f, .55f),
                                radius = 2.dp.toPx(),
                                center = armEnd,
                                blendMode = BlendMode.Lighten
                            )

                        // Dial centre
                        drawCircle(
                            selectionColour,
                            radius = 4.dp.toPx(),
                        )
                    }
                }
            ) { measurables, constraints ->
                val relaxed = constraints.copy(minWidth = 0, minHeight = 0)

                val labelPlaceables = measurables
                    .filter { it.layoutId is String }
                    .associateBy { (it.layoutId as String) }
                    .filterKeys { it.contains(Regex("^label-\\d+$")) }
                    .mapKeys { it.key.removePrefix("label-").toInt() }
                    .mapValues { it.value.measure(relaxed) }
                    .toSortedMap()

                val arm = measurables
                    .first { it.layoutId == "arm" }
                    .measure(Constraints.fixed(diameter.roundToPx(), diameter.roundToPx()))


                layout(diameter.roundToPx(), diameter.roundToPx()) {
                    arm.place(IntOffset.Zero)

                    labelPlaceables.forEach { (position, placeable) ->
                        val ring  = getRing(position)
                        val angle = getAngle(position).toFloat()
                        val x = run {
                            val pos = radius * (1 + sin(angle))
                            val sizeCorrection = placeable.width.toDp()  * .5f * (sin(angle - PI.toFloat()) - 1)
                            val ringOffset = -ringWidth * ring * sin(angle)
                            val padding = - paddingRadius * sin(angle)
                            pos + sizeCorrection + padding + ringOffset
                        }
                        val y = run {
                            val pos = radius * (1 - cos(angle))
                            val sizeCorrection = placeable.height.toDp() * .5f * (cos(angle) - 1)
                            val ringOffset = -ringWidth * ring * (-cos(angle))
                            val padding = paddingRadius * cos(angle)
                            pos + sizeCorrection + padding + ringOffset
                        }

                        placeable.place(
                            x = x.roundToPx(),
                            y = y.roundToPx()
                        )
                    }
                }
            }
        }

        /**
         * Provides an hour dial which the user may interact with
         */
        @Composable fun HourDial(
            value: Int,
            onValueChange: (Int) -> Unit,
            modifier: Modifier = Modifier,
            is24hr: Boolean,
            colour: TimePickerColour,
            interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
        ) = Dial(
            value = when {
                value == 12 -> 0
                is24hr && value == 0 -> 12
                else -> value
            },
            steps = if(is24hr) 24 else 12,
            rings = if(is24hr) 2 else 1,
            onValueChange = if(!is24hr) onValueChange else { {
                onValueChange(when(it) {
                    0 -> 12
                    12 -> 0
                    else -> it
                })
            } },
            labels =
                if(is24hr) (0..23).associateWith {
                    DateTimeFormatter.ofPattern("k" ).format(LocalTime.of(when(it) {
                        0 -> 12
                        12 -> 0
                        else -> it
                    }, 0))
                }
                else (0..11).associateWith { DateTimeFormatter.ofPattern("H").format(LocalTime.of(it, 0)) },
            modifier = modifier,
            colour = colour,
            interactionSource = interactionSource
        )

        @Composable fun MinuteDial(
            value: Int,
            onValueChange: (Int) -> Unit,
            modifier: Modifier = Modifier,
            colour: TimePickerColour,
            interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
        ) = Dial(
            value = value,
            steps = 60,
            onValueChange = onValueChange,
            labels = (0..11).associate {
                it * 5 to NumberFormat.getInstance().format(it * 5)
            },
            modifier = modifier,
            colour = colour,
            interactionSource = interactionSource
        )

        /**
         * Used to allow the user to select between AM and PM
         *
         * @param value           The current value for the time of day
         * @param onValueSelected Called when the user makes a selection
         * @param modifier        Modifier to be applied to the layout
         */
        @Composable fun AMPMSelector(
            value: DayDivision,
            onValueSelected: (DayDivision) -> Unit,
            modifier: Modifier = Modifier,
            colour: TimePickerColour,
        ) {
            // TODO: Handle orientation
            // TODO: Move colours into a single source of truth

            val borderColour = colour.outlineColour
            val selectionColour by colour.backgroundColour(selection = true)
            val selectionTextColour by colour.textColour(selection = true)
            val textColour by colour.textColour()

            fun Modifier.selectionBackground(isSelection: Boolean, shape: Shape) = this.clip(shape).run {
                if(isSelection) background(selectionColour, shape)
                else this
            }

            Column(
                modifier
                    .selectableGroup()
                    .size(
                        width = 52.dp,
                        height = 80.dp
                    )
                    .border(
                        width = 1.dp,
                        color = borderColour,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .selectionBackground(
                            value == DayDivision.AM,
                            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .selectable(
                            selected = value == DayDivision.AM,
                            onClick = { onValueSelected(DayDivision.AM) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.button,
                        LocalContentColor provides if(value == DayDivision.AM) selectionTextColour else textColour
                    ) {
                        Text("AM")
                    }
                }
                Divider(color = borderColour)
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .selectionBackground(
                            value == DayDivision.PM,
                            RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                        )
                        .selectable(
                            selected = value == DayDivision.PM,
                            onClick = { onValueSelected(DayDivision.PM) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.button,
                        LocalContentColor provides if(value == DayDivision.PM) selectionTextColour else textColour
                    ) { Text("PM") }
                }
            }

        }
    }

    @Composable private fun DialogLayout(
        shape: Shape,
        colour: TimePickerColour,
        editMode: EditMode,
        onEditModeChange: (EditMode) -> Unit,
        onDismissRequest: () -> Unit,
        title: @Composable () -> Unit,
        textField: @Composable RowScope.() -> Unit,
        dial: @Composable () -> Unit,
        buttons: @Composable RowScope.() -> Unit
    ) = Dialog(
        onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) { Surface(
        shape = shape,
        color = colour.backgroundColour().value
    ) {
        Column {
            Box(
                Modifier
                    .padding(start = 12.dp)
                    .paddingFromBaseline(
                        top = 28.dp,
                        bottom = 24.dp
                    )
            ) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    ProvideTextStyle(MaterialTheme.typography.caption, title)
                }
            }

            ProvideTextStyle(
                LocalTextStyle.current.copy(
                    fontSize  = TextUnit(24f, TextUnitType.Sp),
                    textAlign = TextAlign.Center
                )
            ) {
                Row(
                    Modifier
                        .padding(horizontal = 24.dp)
                        .align(Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    textField()
                }
            }
            if(editMode == EditMode.Picker) Box(
                Modifier
                    .padding(horizontal = 36.dp)
                    .padding(top = 36.dp, bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                dial()
            }
            Box(
                Modifier
                    .width(328.dp)
                    .padding(
                        top = 8.dp,
                        bottom = 8.dp,
                        start = 16.dp,
                        end = 8.dp,
                    )
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    onClick = {
                        onEditModeChange(when(editMode) {
                            EditMode.Keyboard -> EditMode.Picker
                            EditMode.Picker   -> EditMode.Keyboard
                        })
                    }
                ) {
                    Icon(Icons.Default.Keyboard, null)
                }
                Row(
                    Modifier.align(Alignment.CenterEnd),
                    content = buttons
                )
            }
        }
    } }

    @Composable fun Dialog(
        value: LocalTime,
        onValueSelected: (LocalTime) -> Unit,
        onDismissRequest: () -> Unit,
        modifier: Modifier = Modifier,
        title: @Composable () -> Unit = { Text("Select Time") },
        is24hr: Boolean = DateFormat.is24HourFormat(LocalContext.current),
        shape: Shape = MaterialTheme.shapes.medium,
        colour: TimePickerColour = TimePickerDefault.colour()
    ) {
        // TODO: Seems as though there is bug in compose which causes the focus requesters
        //       to be in an invalid state when created like this
        //       Once fixed replace with is as its less verbose
        //val (hourFocus, minuteFocus) = remember { FocusRequester.createRefs() }
        val hourFocus   = remember { FocusRequester() }
        val minuteFocus = remember { FocusRequester() }

        val hourInteractionSource = remember { MutableInteractionSource() }
        val minuteInteractionSource = remember { MutableInteractionSource() }

        val (editMode, onEditModeChange) = rememberSaveableMutableState(EditMode.Picker)
        var selection by rememberSaveableMutableState(value)

        DialogLayout(
            shape,
            colour,
            editMode,
            onEditModeChange,
            onDismissRequest,
            title = title,
            textField = {
                if(is24hr) NumberField(
                    modifier = Modifier
                        .focusRequester(hourFocus)
                        .size(
                            width  = 96.dp,
                            height = 80.dp
                        ),
                    label = { Text("Hour") },
                    value = selection.hour,
                    onValueChange = {
                        // if the user provides value > 12 should treat as 24hr time
                        // Otherwise handle as 12hr time
                        selection.withHour(it)
                    },
                    onFormatError = { value, e -> },
                    readOnly = editMode == EditMode.Picker,
                    interactionSource = hourInteractionSource
                )
                else NumberField(
                    modifier = Modifier
                        .focusRequester(hourFocus)
                        .size(
                            width  = 96.dp,
                            height = 80.dp
                        ),
                    label = { Text("Hour") },
                    value = selection.get(ChronoField.HOUR_OF_AMPM).let { if(it == 0) 12 else it },
                    onValueChange = {
                        // if the user provides value > 12 should treat as 24hr time
                        // Otherwise handle as 12hr time
                        selection = if(it <= 12) selection.with(ChronoField.HOUR_OF_AMPM, (if(it == 12) 0 else it).toLong())
                        else selection.withHour(it)
                    },
                    onFormatError = { value, e -> },
                    readOnly = editMode == EditMode.Picker,
                    interactionSource = hourInteractionSource
                )
                Text(
                    modifier = Modifier.width(24.dp),
                    text = ":"
                )
                NumberField(
                    modifier = Modifier
                        .focusRequester(minuteFocus)
                        .size(
                            width  = 96.dp,
                            height = 80.dp
                        ),
                    label = { Text("Minute") },
                    value = selection.minute,
                    onValueChange = { selection = selection.withMinute(it) },
                    format = "%02d",
                    onFormatError = { value, e -> },
                    readOnly = editMode == EditMode.Picker,
                    interactionSource = minuteInteractionSource
                )

                if(!is24hr) {
                    Spacer(Modifier.width(12.dp))

                    Components.AMPMSelector(
                        value = if (selection.get(ChronoField.AMPM_OF_DAY) == 0) DayDivision.AM else DayDivision.PM,
                        onValueSelected = {
                            selection = selection.with(ChronoField.AMPM_OF_DAY, it.ordinal.toLong())
                        },
                        colour = colour
                    )
                }
            },
            dial = {
                val hourDialInteractionSource = remember { MutableInteractionSource() }
                val minuteFocused by minuteInteractionSource.collectIsFocusedAsState()

                LaunchedEffect(hourDialInteractionSource) {
                    hourDialInteractionSource.interactions.collect {
                        if(it is DragInteraction.Stop)
                            minuteFocus.requestFocus()
                    }
                }

                when {
                    minuteFocused -> Components.MinuteDial(
                        value = selection.minute,
                        onValueChange = { selection = selection.withMinute(it) },
                        colour = colour
                    )
                    else -> Components.HourDial(
                        value = if(is24hr) selection.hour else selection[ChronoField.HOUR_OF_AMPM],
                        onValueChange = {
                            selection = if(is24hr) selection.withHour(it)
                            else selection.with(ChronoField.HOUR_OF_AMPM, it.toLong())
                        },
                        interactionSource = hourDialInteractionSource,
                        is24hr = is24hr,
                        colour = colour
                    )
                }
            },
            buttons = {
                Button(onClick = onDismissRequest) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onValueSelected(selection) }) { Text("OK") }
            }
        )

        LaunchedEffect(Unit) {
            hourFocus.requestFocus()
        }
    }
}
