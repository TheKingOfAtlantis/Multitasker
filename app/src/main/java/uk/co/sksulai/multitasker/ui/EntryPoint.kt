package uk.co.sksulai.multitasker.ui

import androidx.compose.runtime.*

import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import androidx.datastore.preferences.core.emptyPreferences
import uk.co.sksulai.multitasker.util.DatastoreLocators.AppState

@Composable fun EntryPoint(
    windowSize: WindowSize,
    navController: NavHostController = rememberNavController(),
) {
    val appState by AppState.retrieve().data.collectAsState(emptyPreferences())
    val initialRoute = when {
        !(appState[AppState.OnBoarded] ?: false) -> Destinations.SignInFlow.route
        else -> Destinations.SignInFlow.route
    }

    NavHost(navController, initialRoute) {
        navigation(
            route = Destinations.SignInFlow.route,
            startDestination = when {
                !(appState[AppState.OnBoarded] ?: false) -> Destinations.OnBoarding.route
                else -> Destinations.SignIn.route
            }
        ) {
            composable(Destinations.OnBoarding.route) { /*OnBoardingScreen(navController)*/ }
            composable(Destinations.SignIn.route) { /*SignInScreen(navController)*/ }
            composable(Destinations.SignUp.route) { /*SignUpScreen(navController)*/ }
            composable(Destinations.Forgot.route) { /*ForgotScreen(navController)*/ }
        }
    }
}
