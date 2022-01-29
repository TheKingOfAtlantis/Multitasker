package uk.co.sksulai.multitasker.ui.screen.calendar

import java.util.*
import java.time.*

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*

import androidx.hilt.navigation.compose.hiltViewModel

import uk.co.sksulai.multitasker.util.*
import uk.co.sksulai.multitasker.db.model.*
import uk.co.sksulai.multitasker.db.viewmodel.CalendarViewModel
import uk.co.sksulai.multitasker.ui.component.*
import uk.co.sksulai.multitasker.ui.component.graphics.*

/**
 * Used to sub-categorise events within a calendar
 */
val EventCategories = listOf(
    "Default",
    "Leisure",          // General leisure events
    "Leisure/Food",     // Food events: Dinner/Lunch with friends
    "Leisure/Music",    // Music events: Concerts
    "Leisure/FilmTV",   // Film/TV: Cinema
    "Leisure/Shopping", // Shopping
    "Leisure/Party",    // Party
    "Work",             // General work category
    "Work/Meeting",     // Work meeting
    "Other/Medical",    // Medical event
    "Other/Travel",     // Travel: Flight, train
    "Other/Religious",  // Religious holiday: Christmas, Eid, etc.
)

/**
 * Provides the layout for event creation and editing
 *
 * @param calendar    The calendar which the event is a part of
 * @param colour      The current colour of the event ([EventModel.colour])
 * @param description The current event description ([EventModel.description])
 * @param category    The current category of the event ([EventModel.category])
 * @param location    The current location of the event ([EventModel.location])
 * @param tags        The current list of tags of the event
 * @param start       The current start of the event ([EventModel.start])
 * @param end         The current end of the event ([EventModel.end])
 * @param allDay      Whether this event lasts all day or not ([EventModel.allDay])
 *
 * @param onCalendarChange    Called when the user selects a calendar
 * @param onColourChange      Called when the user selects a colour
 * @param onNameChange        Called when the user provides a new name
 * @param onDescriptionChange Called when the user provides a new description
 * @param onCategoryChange    Called when the user chooses a new category
 * @param onCategoryChange    Called when the user changes the location
 * @param onTagsChange        Called when the user updates the list of tags
 * @param onStartChange       Called when the user changes the start date/time
 * @param onEndChange         Called when the user changes the end date/time of the event
 * @param onAllDayChange      Called when the user has changed if its an all-day event
 *
 * @param childrenEditor Composable which is provided to edit the children.
 *                       This allows both the editor and creation layouts to provide alternative
 *                       content as its not possible to attach children to an event which does not
 *                       yet exist in the database
 *
 * @param onSaveRequest    The user has requested to save the event
 * @param onDismissRequest The user has dismissed the editor dialog
 *
 * @param calendarViewModel
 */
@ExperimentalMaterialApi
@Composable private fun EventEditorLayout(
    calendar: CalendarModel?,
    colour: NamedColour?,
    name: String,
    description: String,
    category: String,
    location: String,
    tags: List<String>,
    start: ZonedDateTime,
    end: ZonedDateTime,
    allDay: Boolean,

    onCalendarChange: (CalendarModel?) -> Unit,
    onColourChange: (NamedColour?) -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onTagsChange: (List<String>) -> Unit,
    onStartChange: (ZonedDateTime) -> Unit,
    onEndChange: (ZonedDateTime) -> Unit,
    onAllDayChange: (Boolean) -> Unit,

    childrenEditor: @Composable ColumnScope.() -> Unit,

    onSaveRequest: () -> Unit,
    onDismissRequest: () -> Unit,
    
    calendarViewModel: CalendarViewModel = hiltViewModel()
) {
    Column {
        TopAppBar(
            backgroundColor = Color.Transparent,
            elevation = 0.dp
        ) {
            IconButton(
                onClick = onDismissRequest,
                content = { Icon(Icons.Default.Close, contentDescription = null) }
            )
            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = onSaveRequest,
                content = { Icon(Icons.Default.Save, contentDescription = null) }
            )
        }

        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val calendars by calendarViewModel.calendars.collectAsState(initial = emptyList())

            Dropdown(
                label = { Text("Calendar") },
                value = calendar,
                onValueSelected = onCalendarChange,
                entries = calendars,
                itemText = { it?.name ?: "" },
                leadingIcon = {
                    ColourIcon(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(24.dp),
                        colour = calendar?.uiColour ?: Color.Unspecified
                    )
                }
            ) {
                ListItem(
                    text = { Text(it.name) },
                    secondaryText = it.description
                        .takeIf { it.isNotEmpty() }
                        ?.let { @Composable { Text(it) } },
                    icon = {
                        ColourIcon(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(24.dp),
                            colour = calendar?.uiColour ?: Color.Unspecified
                        )
                    }
                )
            }

            OutlinedTextField(
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                label = { Text("Name") },
                value = name,
                onValueChange = onNameChange
            )

            OutlinedTextField(
                modifier = Modifier.padding(top = 8.dp),
                label = { Text("Description") },
                value = description,
                onValueChange = onDescriptionChange
            )

            ColourDropdown(
                modifier = Modifier.padding(top = 8.dp),
                value = colour ?: (calendar?.uiColour ?: Color.Unspecified).asNamedColour().copy(name = "Follow Calendar"),
                onValueChange = { onColourChange(it) },
                header = { DropdownMenuItem({ onColourChange(null) }) {
                    val calendarColour = (calendar?.uiColour ?: Color.Unspecified).asNamedColour()
                    ListItem(
                        icon = { ColourIcon(calendarColour.colour,
                            Modifier
                                .padding(vertical = 8.dp)
                                .size(24.dp)) },
                        text = { Text("Calendar Colour") },
                        secondaryText = { Text(calendarColour.name) },
                    )
                } }
            )

            Dropdown(
                modifier = Modifier.padding(top = 8.dp),
                label = { Text("Category") },
                value = category,
                onValueSelected = onCategoryChange,
                entries = EventCategories,
            )

            OutlinedTextField(
                modifier = Modifier.padding(top = 8.dp),
                label = { Text("Tags") },
                value = tags.joinToString(" #"),
                onValueChange = { onTagsChange(it.split(" #")) }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Provides the UI necessary for creating events
 *
 * @param onDismissRequest The user has requested that event creation UI be dismissed
 * @param calendarViewModel Used to provide access to the database
 */
@ExperimentalMaterialApi
@Composable fun EventCreation(
    onDismissRequest: () -> Unit,
    calendarViewModel: CalendarViewModel = hiltViewModel()
) {
    val calendars by calendarViewModel.calendars.collectAsState(emptyList())
    
    var calendar: CalendarModel? by rememberSaveableMutableState(calendars.firstOrNull(), calendars)
    var colour: NamedColour?     by rememberSaveableMutableState(null)
    var name: String             by rememberSaveableMutableState("")
    var description: String      by rememberSaveableMutableState("")
    var category: String         by rememberSaveableMutableState(EventCategories[0])
    var location: String         by rememberSaveableMutableState("")
    var tags: List<String>       by rememberSaveableMutableState(emptyList())
    var start: ZonedDateTime     by rememberSaveableMutableState(ZonedDateTime.now())
    var end: ZonedDateTime       by rememberSaveableMutableState(start + Duration.ofHours(1))
    var allDay: Boolean          by rememberSaveableMutableState(false)

    EventEditorLayout(
        calendar    = calendar,
        colour      = colour,
        name        = name,
        description = description,
        category    = category,
        location    = location,
        tags        = tags,
        start       = start,
        end         = end,
        allDay      = allDay,

        onCalendarChange    = { calendar    = it },
        onColourChange      = { colour      = it },
        onNameChange        = { name        = it },
        onDescriptionChange = { description = it },
        onCategoryChange    = { category    = it },
        onLocationChange    = { location    = it },
        onTagsChange        = { tags        = it },
        onStartChange       = { start       = it },
        onEndChange         = { end         = it },
        onAllDayChange      = { allDay      = it },

        childrenEditor = {
            Text("Save first to add nested events")
            Button(
                content = { Text("Save and add nested event") },
                onClick = {
                    // TODO: Save the event and the provide UI to add children
                }
            )
        },

        onSaveRequest = provideInScope(rememberCoroutineScope()) {
            // If all day which chosen we need to coarse the values for the start and end to be
            // at the start and end of the day (as required)
            // eg. if start and end on the same day => need actual start to be 00:00:01 and end to be 23:59:59

            val actualStart = if (!allDay) start else start.toLocalDate().atStartOfDay(start.zone)
            val actualEnd   = if (!allDay) end   else end.toLocalDate().atTime(LocalTime.MAX).atZone(end.zone)

            calendarViewModel.createEvent(
                calendar    = calendar!!,
                parentID    = null,
                colour      = colour?.colour,
                name        = name,
                description = description,
                category    = category,
                location    = location,
                tags        = tags,
                allDay      = allDay,
                start       = actualStart,
                duration    = Duration.between(actualStart, actualEnd),
                endTimeZone = actualEnd.timezone,
            )
            onDismissRequest()
        },
        onDismissRequest  = onDismissRequest,
        calendarViewModel = calendarViewModel
    )

}
