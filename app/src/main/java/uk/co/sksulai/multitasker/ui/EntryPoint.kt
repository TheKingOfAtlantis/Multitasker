package uk.co.sksulai.multitasker.ui

import android.content.Context

import androidx.compose.runtime.*
import androidx.compose.animation.Crossfade
import androidx.compose.ui.platform.LocalContext

import androidx.navigation.*
import androidx.navigation.compose.*

import androidx.datastore.preferences.core.preferencesOf
import androidx.lifecycle.viewmodel.compose.viewModel

import uk.co.sksulai.multitasker.util.*
import uk.co.sksulai.multitasker.ui.userFlow.*
import uk.co.sksulai.multitasker.db.viewmodel.UserViewModel
import uk.co.sksulai.multitasker.util.Datastores.appStatePref

@Composable fun EntryPoint(
    navController: NavHostController = rememberNavController(),
    userViewModel: UserViewModel     = viewModel(),
    context: Context                 = LocalContext.current,
) {
    val statePref   by context.appStatePref.data.collectAsState(initial = preferencesOf())
    val currentUser by userViewModel.currentUser.collectAsState(initial = null)

    val initialState = remember(statePref, currentUser) {
        when {
            // Check if this is the first launch
            statePref[DatastoreKeys.AppState.OnBoarded]?.not() ?: true -> "OnBoarding"
            // Check if a user is currently signed in, if not direct to sign in/sign up
            !statePref[DatastoreKeys.AppState.CurrentUser].isNullOrEmpty() -> "SignInFlow"
            // Otherwise direct to calendar view
            else -> "CalendarView"
        }
    }
    NavHost(navController, initialState) {
        composable("OnBoarding") { OnBoardingScreen(navController) }
        navigation(startDestination = "SignIn", route = "SignInFlow") {
            composable("SignIn") { SignInScreen(navController) }
            composable("Forgot") { }
        }
    }
}
