package uk.co.sksulai.multitasker.ui.screen.signin

import java.text.*
import java.time.*
import java.util.*
import java.time.format.DateTimeFormatter

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
import uk.co.sksulai.multitasker.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable fun SignUpScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel()
) = Surface {
    val pageController = rememberNavController()

    val user by userViewModel.currentUser.collectAsState(initial = null)

    NavHost(
        navController = pageController,
        startDestination =
            if(userViewModel.emailVerification.verified) UserDetailsRoute
            else EmailVerificationRoute
    ) {
        EmailVerificationPage( // Need to verify user email
            user?.Email ?: "",
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
    email: String,
    emailVerification: UserRepository.EmailVerification
) = composable(EmailVerificationRoute) {
    val scope = rememberCoroutineScope()
    var linkSent by rememberSaveableMutableState(false)

    if(!linkSent) EmailVerification(
        email,
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
        value = updatedUser?.DisplayName ?: "",
        onValueChange = {
            updatedUser = updatedUser?.copy(DisplayName = it)
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
        value = Destinations.calendarDestinations.find { it.route == updatedUser?.PreferredHome },
        onValueSelected = { updatedUser = updatedUser?.copy(PreferredHome = it.route) },
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
        value = updatedUser?.Home ?: "",
        onValueChange = { updatedUser = updatedUser?.copy(Home = it) }
    )
    HelperText(
        "Used to suggest when best to leave",
        modifier = Modifier
            .textFieldHelperPadding()
            .padding(bottom = 4.dp)
    )


    // TODO: Add date picker
    val dobFormat = remember {
        val dateFormat = SimpleDateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()) as SimpleDateFormat
        dateFormat.timeZone = TimeZone.getDefault()
        var format = dateFormat.toPattern()

        // Fix the size of each date part
        if(format.replace(Regex("[^d]"), "").length == 1)
            format = format.replace(Regex("d+"), "dd")
        if(format.replace(Regex("[^M]"), "").length == 1)
            format = format.replace(Regex("M+"), "MM")
        if(format.replace(Regex("[^y]"), "").length < 4)
            format = format.replace(Regex("y+"), "yyyy")

        format
    }
    val dobHint = remember(dobFormat) {
        // Based on getTextInputHint(...) in https://github.com/material-components/material-components-android/
        // Get the date format symbols
        val symbols = DateFormatSymbols.getInstance(Locale.ROOT).localPatternChars
        val localisedSymbols = DateFormatSymbols.getInstance(Locale.getDefault()).localPatternChars
        // Generate a mapping between localised and unlocalised version
        val symbolsMap = localisedSymbols
            .mapIndexed { index, c  ->  c to symbols[index] }
            .toMap()
        dobFormat.map {
            if(!symbolsMap.contains(it)) it // Keep symbols we cannot map
            else symbolsMap.getValue(it)    // Map to localised date symbol
        }
        .joinToString("")           // Join list together into string
        .uppercase(Locale.getDefault())     // Then make uppercase
    }
    val formatter = DateTimeFormatter.ofPattern(dobFormat)

    var dobValue by rememberSaveableMutableState("")
    var dobError by rememberSaveableMutableState("")
    OutlinedTextField(
        modifier = Modifier.padding(vertical = 2.dp),
        label = { LabelText("Birthday", dobError.isNotEmpty() && dobValue.isNotEmpty()) },
        value = dobValue,
        onValueChange = {
            dobValue = it
            try {
                updatedUser = updatedUser?.copy(DOB = LocalDate.parse(it, formatter))
                dobError = ""
            } catch(e: DateTimeException) {
                dobError = "Birthday poorly formatted"
            }
        },
        placeholder = { Text(dobHint) },
        isError = dobError.isNotEmpty(),
        singleLine = true
    )
    HelperText(
        text  = "Add it to your calendar! So we can celebrate together",
        error = dobError,
        modifier = Modifier
            .textFieldHelperPadding()
            .padding(bottom = 4.dp)
    )

    val scope = rememberCoroutineScope()
    Button(
        modifier = Modifier
            .padding(top = 4.dp)
            .align(Alignment.CenterHorizontally)
        ,
        content = { Text("Submit") },
        onClick = provideInScope(scope) {
            if(updatedUser?.DisplayName.isNullOrEmpty())
                displayNameError = "Display name is required"
            else {
                updatedUser?.let { userViewModel.update(it) }
                navigateNext()
            }
        }
    )
} }
