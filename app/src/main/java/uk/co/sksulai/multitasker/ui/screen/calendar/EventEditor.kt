package uk.co.sksulai.multitasker.ui.screen.calendar

import java.util.*
import java.time.*

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier

import androidx.hilt.navigation.compose.hiltViewModel

import uk.co.sksulai.multitasker.util.*
import uk.co.sksulai.multitasker.db.model.*
import uk.co.sksulai.multitasker.db.viewmodel.CalendarViewModel
import uk.co.sksulai.multitasker.ui.component.graphics.NamedColour

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
 * @param duration    The current duration of the event ([EventModel.duration])
 * @param allDay      Whether this event lasts all day or not
 * @param endTimezone The timezone to be associated with the end ([EventModel.endTimezone])
 *
 * @param onCalendarChange    Called when the user selects a calendar
 * @param onColourChange      Called when the user selects a colour
 * @param onNameChange        Called when the user provides a new name
 * @param onDescriptionChange Called when the user provides a new description
 * @param onCategoryChange    Called when the user chooses a new category
 * @param onCategoryChange    Called when the user changes the location
 * @param onTagsChange        Called when the user updates the list of tags
 * @param onStartChange       Called when the user changes the start date/time
 * @param onDurationChange    Called when the user changes the duration of the event
 * @param onAllDayChange      Called when the user has changed if its an all-day event
 * @param onEndTimezoneChange Called when the timezone of the end has changed
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
@Composable private fun EventEditorLayout(
    calendar: CalendarModel?,
    colour: NamedColour?,
    name: String,
    description: String,
    category: String,
    location: String,
    tags: List<String>,
    start: ZonedDateTime,
    duration: Duration,
    allDay: Boolean,
    endTimezone: TimeZone,

    onCalendarChange: (CalendarModel?) -> Unit,
    onColourChange: (NamedColour?) -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onTagsChange: (List<String>) -> Unit,
    onStartChange: (ZonedDateTime) -> Unit,
    onDurationChange: (Duration) -> Unit,
    onAllDayChange: (Boolean) -> Unit,
    onEndTimezoneChange: (TimeZone) -> Unit,

    childrenEditor: @Composable ColumnScope.() -> Unit,

    onSaveRequest: () -> Unit,
    onDismissRequest: () -> Unit,
    
    calendarViewModel: CalendarViewModel = hiltViewModel()
) {
}

/**
 * Provides the UI necessary for creating events
 *
 * @param onDismissRequest The user has requested that event creation UI be dismissed
 * @param calendarViewModel Used to provide access to the database
 */
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
    var duration: Duration       by rememberSaveableMutableState(Duration.ofHours(1))
    var allDay: Boolean          by rememberSaveableMutableState(false)
    var endTimezone: TimeZone    by rememberSaveableMutableState(TimeZone.getDefault())

    EventEditorLayout(
        calendar    = calendar,
        colour      = colour,
        name        = name,
        description = description,
        category    = category,
        location    = location,
        tags        = tags,
        start       = start,
        duration    = duration,
        allDay      = allDay,
        endTimezone = endTimezone,

        onCalendarChange    = { calendar    = it },
        onColourChange      = { colour      = it },
        onNameChange        = { name        = it },
        onDescriptionChange = { description = it },
        onCategoryChange    = { category    = it },
        onLocationChange    = { location    = it },
        onTagsChange        = { tags        = it },
        onStartChange       = { start       = it },
        onDurationChange    = { duration    = it },
        onAllDayChange      = { allDay      = it },
        onEndTimezoneChange = { endTimezone = it },

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
            calendarViewModel.createEvent(
                calendar    = calendar!!,
                parentID    = null,
                colour      = colour?.colour,
                name        = name,
                description = description,
                category    = category,
                location    = location,
                tags        = tags,
                start       = start,
                duration    = duration,
                allDay      = allDay,
                endTimeZone = endTimezone
            )
            onDismissRequest()
        },
        onDismissRequest  = onDismissRequest,
        calendarViewModel = calendarViewModel
    )

}
