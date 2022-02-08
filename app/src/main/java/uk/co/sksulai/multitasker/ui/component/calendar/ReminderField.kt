package uk.co.sksulai.multitasker.ui.component.calendar

import java.time.Duration

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

import uk.co.sksulai.multitasker.ui.component.NumberField
import uk.co.sksulai.multitasker.util.rememberSaveableMutableState

enum class DurationUnit {
    Minutes,
    Hours,
    Days,
    Weeks,
}

fun getDurationString(duration: Duration) = when {
    // TODO: Find localisable version this
    // TODO: Fix plural
    duration.toDays() > 6 &&
    duration.toDays() % 7 == 0L -> "${duration.toDays()/7} weeks before"
    duration.toDays() > 0       -> "${duration.toDays()} days before"
    duration.toHours() > 0      -> "${duration.toHours()} hours before"
    duration.toMinutes() > 0    -> "${duration.toMinutes()} minutes before"
    else -> throw IllegalArgumentException()
}

@ExperimentalMaterialApi
@Composable private fun CustomReminder(
    onValueSelected: (Duration) -> Unit,
    onDismissRequest: () -> Unit
) = Dialog(onDismissRequest) { Surface(
    shape = MaterialTheme.shapes.medium
) {
    Column(Modifier.padding(
        horizontal = 8.dp,
        vertical = 16.dp
    )) {
        var input by rememberSaveableMutableState(1)

        Box(
            Modifier
                .padding(
                    top = 24.dp,
                    bottom = 24.dp,
                    start = 16.dp
                )
        ) {
            Text("Custom Reminder")
        }

        NumberField(
            value = input,
            onValueChange = { input = it },
            onFormatError = { str, e -> },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
        )

        var durationUnit by rememberSaveableMutableState(DurationUnit.Minutes)
        Column(
            Modifier
                .padding(start = 16.dp)
                .selectableGroup()
        ) {
            @Composable
            fun option(
                value: DurationUnit,
                text: @Composable () -> Unit
            ) = Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = durationUnit == value,
                    onClick = { durationUnit = value }
                )
                Box(Modifier.padding(start = 8.dp)) {
                    text()
                }
            }

            @Composable
            fun option(value: DurationUnit) = option(value) { Text(value.name + " before") }

            DurationUnit.values().forEach { option(it) }
        }

        Row(
            Modifier
                .padding(
                    top = 8.dp,
                    end = 8.dp,
                    bottom = 8.dp
                )
                .align(Alignment.End)
        ) {
            TextButton(onClick = onDismissRequest) { Text("Cancel") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                onValueSelected(
                    when (durationUnit) {
                        DurationUnit.Minutes -> Duration.ofMinutes(input.toLong())
                        DurationUnit.Hours   -> Duration.ofHours(input.toLong())
                        DurationUnit.Days    -> Duration.ofDays(input.toLong())
                        DurationUnit.Weeks   -> Duration.ofDays(7 * input.toLong())
                    }
                )
            }) { Text("Add") }
        }
    }
} }

@ExperimentalMaterialApi
@Composable fun ReminderList(
    onValueSelected: (Duration) -> Unit,
    onDismissRequest: () -> Unit,
    onCustomRequest: () -> Unit,
) = Dialog(onDismissRequest) { Surface(
    shape = MaterialTheme.shapes.medium
) {
    Column(
        Modifier.padding(horizontal = 8.dp)
    ) {
        // TODO: Cache user created reminders
        val durations = listOf(
            Duration.ofMinutes(5),
            Duration.ofMinutes(10),
            Duration.ofMinutes(15),
            Duration.ofMinutes(30),
            Duration.ofHours(1),
//            Duration.ofHours(12),
            Duration.ofDays(1),
//            Duration.ofDays(7),
        )

        LazyColumn {
            items(durations) {
                ListItem(
                    modifier = Modifier.clickable { onValueSelected(it) },
                    text = { Text(getDurationString(it)) },
                )
            }
            item {
                ListItem(
                    modifier = Modifier.clickable(onClick = onCustomRequest),
                    text = { Text("Custom") },
                )
            }
        }
    }
} }

@ExperimentalMaterialApi
@Composable fun ReminderDialog(
    onValueSelected: (Duration) -> Unit,
    onDismissRequest: () -> Unit
) {
    var showCustom by rememberSaveableMutableState(false)

    if(!showCustom) ReminderList(
        onValueSelected,
        onDismissRequest,
        onCustomRequest = { showCustom = true }
    ) else CustomReminder(
        onValueSelected,
        onDismissRequest
    )
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable fun ReminderField(
    reminders: List<Duration>,
    onNewReminder: (Duration) -> Unit,
    onRemoveReminder: (Duration) -> Unit,
) = Column(horizontalAlignment = Alignment.CenterHorizontally) {
    var showReminderDialog by rememberSaveableMutableState(false)

    LazyColumn(
        Modifier.height(56.dp * reminders.size)
    ) {
        items(reminders.sorted(), key = { it.hashCode() }) {
            ListItem(
                modifier = Modifier.padding(horizontal = 32.dp),
                text = { Text(getDurationString(it)) },
                trailing = {
                    IconButton(onClick = { onRemoveReminder(it) }) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            )
        }
    }

    TextButton(
        modifier = Modifier.fillMaxWidth(.8f),
        onClick = { showReminderDialog = true }
    ) {
        Icon(Icons.Default.Add, null)
        Text("Add Reminder")
    }
    if(showReminderDialog) ReminderDialog(
        onValueSelected = {
            onNewReminder(it)
            showReminderDialog = false
        },
        onDismissRequest = { showReminderDialog = false }
    )
}
