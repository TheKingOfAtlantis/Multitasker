package uk.co.sksulai.multitasker.ui.screen.calendar

import java.time.Duration

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.text.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

import androidx.hilt.navigation.compose.hiltViewModel

import uk.co.sksulai.multitasker.util.*
import uk.co.sksulai.multitasker.db.model.*
import uk.co.sksulai.multitasker.db.viewmodel.*
import uk.co.sksulai.multitasker.ui.component.*
import uk.co.sksulai.multitasker.ui.component.graphics.*
import uk.co.sksulai.multitasker.ui.component.calendar.ReminderField

/**
 * Used as the basis for both the creation and editor dialogs. Used to provide all the
 * necessary fields to edit and create a calendar however the logic is deferred to the
 * caller to provide.
 *
 * ___n.b.__ Current [owner] and [onOwnerChange] do nothing_
 *
 * @param owner       The current owner of the calendar
 * @param name        The current name of the calendar
 * @param description The current description of the calendar
 * @param colour      The current colour of the calendar
 *
 * @param onOwnerChange        Callback that is triggered when a new owner has been selected
 * @param onNameChange         Callback that is triggered when the name has been changed
 * @param onDescriptionChange  Callback that is triggered when the description has been changed
 *
 * @param onSaveCalendar Indicates that the user has requested to save the changes
 * @param onDismissRequest Indicates that the user requested to discard the changes and dismiss the editor
 *
 * @param modifier      Modifier to apply to the layout
 * @param userViewModel Viewmodel that is used to retrieve the list of potential owners
 *
 */
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable private fun CalendarEditorLayout(
    owner: UserModel?,
    name: String,
    description: String,
    colour: NamedColour,
    reminders: List<Duration>,

    onOwnerChange: (UserModel) -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onColourChange: (NamedColour) -> Unit,
    onRemindersChange: (List<Duration>) -> Unit,

    onSaveCalendar: () -> Unit,
    onDismissRequest: () -> Unit,

    modifier: Modifier = Modifier,
    userViewModel: UserViewModel = hiltViewModel()
) = Column(
    modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
) {
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
            onClick = onSaveCalendar,
            content = { Icon(Icons.Default.Save, contentDescription = null) }
        )
    }

    val keyboard = LocalSoftwareKeyboardController.current
    val (
        nameFocus,
        descriptionFocus
    ) = FocusRequester.createRefs()

    LaunchedEffect(Unit) { // Start with the name field focused
        nameFocus.requestFocus()
    }

    // TODO: Once we work out how to have multi-user sessions enable it and list those users
    // val userList by userViewModel.getPotentialOwners().collectAsState(emptyList())
    val userList = emptyList<UserModel>()
    Dropdown(
        value = owner,
        onValueSelected = onOwnerChange,
        entries = userList,
        enabled = false,
        itemText = { it?.displayName ?: "" }
    ) {
        ListItem(
            text = { Text(it.displayName ?: "") },
            secondaryText = { Text(it.email ?: "") },
        )
    }

    OutlinedTextField(
        modifier = Modifier
            .padding(top = 8.dp)
            .focusRequester(nameFocus)
        ,
        label = { Text("Calendar Name") },
        value = name,
        onValueChange = onNameChange,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions { descriptionFocus.requestFocus() },
        singleLine = true
    )
    OutlinedTextField(
        modifier = Modifier
            .padding(top = 8.dp)
            .focusRequester(descriptionFocus)
        ,
        label = { Text("Description") },
        value = description,
        onValueChange = onDescriptionChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions { keyboard?.hide() },
    )
    ColourDropdown(
        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
        value = colour,
        onValueChange = { onColourChange(it) },
    )

    Divider(Modifier.padding(vertical = 16.dp))

    ReminderField(
        reminders = reminders,
        onNewReminder = {
            if(it !in reminders)
                onRemindersChange(reminders + it)
        },
        onRemoveReminder = { removed -> onRemindersChange(reminders.filterNot { it == removed }) }
    )

    Spacer(Modifier.height(24.dp))
}

/**
 * The contents of the calendar creation bottom sheet dialog
 *
 * @param onDismissRequest  Callback for when the user requests the dismissal of the dialog
 * @param calendarViewModel Used to create the calendar
 * @param userViewModel     Used to retrieve users
 */
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable fun CalendarCreation(
    onDismissRequest: () -> Unit,
    calendarViewModel: CalendarViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()

    val owner       by userViewModel.currentUser.collectAsState(initial = null)
    var name        by rememberSaveableMutableState("")
    var description by rememberSaveableMutableState("")
    var colour      by rememberSaveableMutableState(DefaultColours.random())
    var reminders   by rememberSaveableMutableState(emptyList<Duration>())

    CalendarEditorLayout(
        owner = owner,
        name  = name,
        description = description,
        colour = colour,
        reminders = reminders,

        onOwnerChange = { /*TODO*/ },
        onNameChange = { name = it },
        onDescriptionChange = { description = it },
        onColourChange = { colour = it },
        onRemindersChange = { reminders = it },

        onSaveCalendar = provideInScope(scope) {
            calendarViewModel.createCalendar(owner!!, name, description, colour.colour, reminders)
            onDismissRequest()
        },
        onDismissRequest = onDismissRequest,
        userViewModel = userViewModel
    )
}
