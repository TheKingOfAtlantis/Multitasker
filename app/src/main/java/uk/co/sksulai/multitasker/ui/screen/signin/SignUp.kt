package uk.co.sksulai.multitasker.ui.screen.signin

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.navigation.*
import androidx.navigation.compose.*
import androidx.hilt.navigation.compose.hiltViewModel

import uk.co.sksulai.multitasker.ui.component.*
import uk.co.sksulai.multitasker.ui.Destinations
import uk.co.sksulai.multitasker.db.repo.UserRepository
import uk.co.sksulai.multitasker.db.viewmodel.UserViewModel
import uk.co.sksulai.multitasker.ui.component.calendar.DateTextField
import uk.co.sksulai.multitasker.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable fun SignUpScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel()
) = Surface {
    val pageController = rememberNavController()

    NavHost(
        navController = pageController,
        startDestination =
            if(userViewModel.emailVerification.verified) UserDetailsRoute
            else EmailVerificationRoute
    ) {
        EmailVerificationPage( // Need to verify user email
            userViewModel.emailVerification
        )
        UserDetailsPage( // Then add the user's details
            navigateNext = { Destinations.CalendarView.navigate(navController) },
            userViewModel
        )
    }
}

const val EmailVerificationRoute = "verify"
fun NavGraphBuilder.EmailVerificationPage(
    emailVerification: UserRepository.EmailVerification
) = composable(EmailVerificationRoute) {
    val scope = rememberCoroutineScope()
    var linkSent by rememberSaveableMutableState(false)

    if(!linkSent) EmailVerification(
        onSubmitRequest = provideInScope(scope) {
            emailVerification.request("signup#details")
            linkSent = true
        },
        preamble = {
            Text(
                "We need to verify you email to prevent spam" +
                    " and to ensure we can reach you if we need to"
            )
        }
    ) else Column(
        Modifier
            .fillMaxSize()
            .wrapContentSize()
    ) {
        Text("Link has been sent")
    }
}

const val UserDetailsRoute = "details"

@ExperimentalMaterialApi
fun NavGraphBuilder.UserDetailsPage(
    navigateNext: () -> Unit,
    userViewModel: UserViewModel,
) = composable(UserDetailsRoute) { Column(
    Modifier
        .fillMaxSize()
        .wrapContentSize()
        .verticalScroll(rememberScrollState())
) {
    val user by userViewModel.currentUser.collectAsState(null)
    var updatedUser by rememberSaveableMutableState(user, user)

    Text(
        "Your Details",
        style = MaterialTheme.typography.h6,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    // TODO: Avatar Picker

    var displayNameError by rememberSaveableMutableState("")
    OutlinedTextField(
        modifier = Modifier,
        label = { Text("Display Name (required)") },
        value = updatedUser?.displayName ?: "",
        onValueChange = {
            updatedUser = updatedUser?.copy(displayName = it)
            displayNameError = ""
        },
        isError = displayNameError.isNotEmpty()
    )
    HelperText(
        "Can be your real name or something else",
        displayNameError,
        modifier = Modifier
            .textFieldHelperPadding()
            .padding(bottom = 4.dp)
    )

    Dropdown(
        modifier = Modifier.padding(vertical = 2.dp),
        label = { Text("Default View") },
        value = Destinations.calendarDestinations.find { it.route == updatedUser?.preferredHome },
        onValueSelected = { updatedUser = updatedUser?.copy(preferredHome = it.route) },
        entries  = Destinations.calendarDestinations,
        itemText = { it?.route ?: "" }
    )
    HelperText(
        "Preferred way to look at a calendar",
        modifier = Modifier
            .textFieldHelperPadding()
            .padding(bottom = 4.dp)
    )

    OutlinedTextField(
        modifier = Modifier.padding(vertical = 2.dp),
        label = { Text("Home Address") },
        value = updatedUser?.home ?: "",
        onValueChange = { updatedUser = updatedUser?.copy(home = it) }
    )
    HelperText(
        "Used to suggest when best to leave",
        modifier = Modifier
            .textFieldHelperPadding()
            .padding(bottom = 4.dp)
    )


    // TODO: Add date picker
    var dobError by rememberSaveableMutableState("")
    DateTextField(
        modifier = Modifier.padding(vertical = 2.dp),
        label = { LabelText(dobError.isNotEmpty(), "Birthday") },
        value = updatedUser?.dob,
        onValueComplete = { updatedUser = updatedUser?.copy(dob = it) },
        onFormatError = { dobError = it },
        isError = dobError.isNotEmpty(),
    )
    HelperText(
        text  = "Add it to your calendar! So we can celebrate together",
        error = dobError,
        modifier = Modifier
            .textFieldHelperPadding()
            .padding(bottom = 4.dp)
    )

    Button(
        modifier = Modifier
            .padding(top = 4.dp)
            .align(Alignment.CenterHorizontally)
        ,
        content = { Text("Submit") },
        onClick = provideInScope(rememberCoroutineScope()) {
            if(updatedUser?.displayName.isNullOrEmpty())
                displayNameError = "Display name is required"
            else {
                updatedUser?.let { userViewModel.update(it) }
                navigateNext()
            }
        }
    )
} }
