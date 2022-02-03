package uk.co.sksulai.multitasker.ui.component.calendar

import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.annotation.IntRange
import java.text.NumberFormat
import java.time.LocalTime
import java.time.temporal.ChronoField
import kotlin.math.*


import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.ui.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*

import uk.co.sksulai.multitasker.ui.component.NumberField
import uk.co.sksulai.multitasker.util.rememberSaveableMutableState

@ExperimentalComposeUiApi
object TimePicker {
    enum class EditMode { Keyboard, Picker }
    enum class DayDivision { AM, PM }

    object Components {

        /**
         * Generic based from which the hour and minute dials built
         *
         * @param position         The current index position of the dial hand (in range 0..[steps])
         * @param onPositionChange Called when the user moves the dial to a new position
         * @param steps            The number of steps around the dial
         * @param labels           Set of labels to be placed at specific positions around the dial
         * @param modifier         Modifier to be applied to the dial
         */
        @Composable fun Dial(
            position: Int,
            onPositionChange: (Int) -> Unit,
            steps: Int,
            labels: Map<Int, String>,
            modifier: Modifier = Modifier
        ) {
            if(position !in 0 until steps) throw IndexOutOfBoundsException(
                "Expected position to be in the range [0, steps: ${steps}) but was: $position"
            )

            val diameter = 256.dp
            val paddingRadius = 16.dp

            val dialBackground = LocalContentColor.current
                .copy(alpha = ContentAlpha.disabled)

            fun getAngle(position: Int) = (2 * PI) * (position.toFloat()/steps)

            Layout(
                modifier = modifier.drawBehind {
                    drawCircle(
                        dialBackground,
                        radius = size.width/2f,
                    )
                }
                .pointerInput(Unit) {
                    forEachGesture {
                        awaitPointerEventScope {
                            fun updatePosition(position: Offset) {
                                val newPosition = position - Offset(diameter.toPx(), diameter.toPx()) / 2f
                                val newAngle    = (newPosition.run { atan2(x, -y) } + 2 * PI) % (2 * PI)

                                onPositionChange((newAngle/(2 * PI) * steps).roundToInt() % steps)
                                // TODO: Handle haptic feedback
                            }

                            val down = awaitFirstDown()
                            updatePosition(down.position)

                            while(true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.first { it.id == down.id }

                                if(change.changedToUpIgnoreConsumed())
                                    break

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
                            modifier = Modifier.layoutId("label-$position")
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

                layout(diameter.roundToPx(), diameter.roundToPx()) {
                    val radius = diameter/2

                    labelPlaceables.forEach { (position, placeable) ->
                        val angle = getAngle(position).toFloat()
                        val paddingRadius = 16.dp

                        val x = run {
                            val pos = radius * (1 + sin(angle))
                            val sizeCorrection = placeable.width.toDp()  * .5f * (sin(angle - PI.toFloat()) - 1)
                            val padding = - paddingRadius * sin(angle)
                            pos + sizeCorrection + padding
                        }
                        val y = run {
                            val pos = radius * (1 - cos(angle))
                            val sizeCorrection = placeable.height.toDp() * .5f * (cos(angle) - 1)
                            val padding = paddingRadius * cos(angle)
                            pos + sizeCorrection + padding
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
            is24hr: Boolean = DateFormat.is24HourFormat(LocalContext.current)
        ) = Dial(
            // TODO: Respect 12hr vs 24hr
            position = if(value == 12) 0 else value,
            steps = 12,
            onPositionChange = onValueChange,
            labels = (0..11).associateWith {
                NumberFormat.getInstance().format(if(it == 0) 12 else it)
            },
            modifier = modifier
        )

        @Composable fun MinuteDial(
            value: Int,
            onValueChange: (Int) -> Unit,
            modifier: Modifier = Modifier
        ) = Dial(
            position = value,
            steps = 60,
            onPositionChange = onValueChange,
            labels = (0..11).associate {
                it * 5 to NumberFormat.getInstance().format(it * 5)
            },
            modifier = modifier
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
        ) {
            // TODO: Handle orientation
            // TODO: Move colours into a single source of truth

            val borderColour = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium)
            val selectionColour = MaterialTheme.colors.primary
            val selectionTextColour = MaterialTheme.colors.contentColorFor(selectionColour)
            val textColour = LocalContentColor.current

            fun Modifier.selectionBackground(isSelection: Boolean, shape: Shape) =
                if(isSelection) this.background(selectionColour, shape)
                else this

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
        editMode: EditMode,
        onEditModeChange: (EditMode) -> Unit,
        onDismissRequest: () -> Unit,
        title: @Composable () -> Unit,
        textField: @Composable RowScope.() -> Unit,
        dial: @Composable () -> Unit,
        buttons: @Composable RowScope.() -> Unit
    ) = Dialog(onDismissRequest) { Surface(
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            title()
            Row {
                textField()
            }
            dial()
            Row {
                buttons()
            }
        }
    } }

    @Composable fun Dialog(
        value: LocalTime,
        onValueSelected: (LocalTime) -> Unit,
        onDismissRequest: () -> Unit,
        title: @Composable () -> Unit = { Text("Select Time") }
    ) {
        val (editMode, onEditModeChange) = rememberSaveableMutableState(EditMode.Picker)

        var selection by rememberSaveableMutableState(value)

        DialogLayout(
            editMode,
            onEditModeChange,
            onDismissRequest,
            title = title,
            textField = {
                NumberField(
                    modifier = Modifier
                        .size(
                            width  = 96.dp,
                            height = 80.dp
                        ),
                    label = { Text("Hour") },
                    value = selection.get(ChronoField.HOUR_OF_AMPM),
                    onValueChange = { selection = selection.withHour(it) },
                    onFormatError = { value, e -> },
                    readOnly = editMode == EditMode.Picker
                )
                Text(
                    modifier = Modifier.width(24.dp),
                    text = ":"
                )
                NumberField(
                    modifier = Modifier
                        .size(
                            width  = 96.dp,
                            height = 80.dp
                        ),
                    label = { Text("Minute") },
                    value = selection.minute,
                    onValueChange = { selection = selection.withHour(it) },
                    format = "%02d",
                    onFormatError = { value, e -> },
                    readOnly = editMode == EditMode.Picker
                )

                Spacer(Modifier.padding(12.dp))

                Components.AMPMSelector(
                    value = if(selection.get(ChronoField.AMPM_OF_DAY) == 0) DayDivision.AM else DayDivision.PM,
                    onValueSelected = {
                        selection = selection.with(ChronoField.AMPM_OF_DAY, it.ordinal.toLong())
                    }
                )
            },
            dial = {
            },
            buttons = {
                Button(onClick = onDismissRequest) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onValueSelected(selection) }) { Text("OK") }
            }
        )
    }
}
