package uk.co.sksulai.multitasker.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.preferencesOf
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.*
import androidx.navigation.compose.*

import uk.co.sksulai.multitasker.util.*
import uk.co.sksulai.multitasker.util.Datastores.appStatePref
import uk.co.sksulai.multitasker.db.viewmodel.UserViewModel
import uk.co.sksulai.multitasker.ui.userFlow.*

@Composable fun EntryPoint(
    navController: NavHostController = rememberNavController(),
    userViewModel: UserViewModel     = viewModel()
) {
    val context     = LocalContext.current

    val statePref   by context.appStatePref.data.collectAsState(initial = preferencesOf())
    val currentUser by userViewModel.currentUser.collectAsState(initial = null)
    val initialState = remember(statePref, currentUser) {
        when {
            // Check if this is the first launch
            statePref[DatastoreKeys.AppState.OnBoarded] ?: false -> "OnBoarding"
            // Check if a user is currently signed in, if not direct to sign in/sign up
            !statePref[DatastoreKeys.AppState.CurrentUser].isNullOrEmpty() -> "SignInFlow"
            // Otherwise direct to calendar view
            else -> "CalendarView"
        }
    }

    NavHost(navController, initialState) {
        composable("OnBoarding") { OnBoardingScreen(navController) }
        navigation("SignIn", "SignInFlow") {
            composable("SignIn") { SignInScreen(navController) }
            composable("SignUp") { SignUpScreen(navController) }
            composable("Forgot") { }
        }
    }
}
