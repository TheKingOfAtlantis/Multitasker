package uk.co.sksulai.multitasker.ui.component.calendar

import java.time.LocalTime
import java.time.temporal.ChronoField

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*

import uk.co.sksulai.multitasker.ui.component.NumberField
import uk.co.sksulai.multitasker.util.rememberSaveableMutableState

object TimePicker {
    enum class EditMode { Keyboard, Picker }
    enum class DayDivision { AM, PM }

    object Components {
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
