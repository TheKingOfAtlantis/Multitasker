package uk.co.sksulai.multitasker.ui.component.calendar

import java.time.LocalTime

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

import uk.co.sksulai.multitasker.util.rememberSaveableMutableState

object TimePicker {
    enum class EditMode { Keyboard, Picker }
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
