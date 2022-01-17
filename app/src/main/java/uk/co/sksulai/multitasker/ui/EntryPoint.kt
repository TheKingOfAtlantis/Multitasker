package uk.co.sksulai.multitasker.ui

import android.net.Uri
import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.*

import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import androidx.datastore.preferences.core.emptyPreferences
import com.google.firebase.ktx.Firebase
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks

import uk.co.sksulai.multitasker.ui.screen.signin.*
import uk.co.sksulai.multitasker.util.*
import uk.co.sksulai.multitasker.util.DatastoreLocators.AppState

const val MultitaskerBaseUrl = "app.multitasker.xyz"

@Composable private fun handleDynamicLinks(
    navController: NavHostController,
    activity: Activity = LocalActivity.current,
    dynamicLinks: FirebaseDynamicLinks = Firebase.dynamicLinks
) = dynamicLinks.getDynamicLink(activity.intent).addOnSuccessListener {
    val deepLink = it?.link
    if(deepLink != null)
        navController.handleDeepLink(Intent().apply { data = deepLink })
}

@Composable fun EntryPoint(
    navController: NavHostController = rememberNavController(),
) {
    val appState by AppState.retrieve().data.collectAsState(emptyPreferences())
    val initialRoute = when {
        !appState[AppState.CurrentUser].isNullOrEmpty() -> Destinations.CalendarView.route
        !(appState[AppState.OnBoarded] ?: false) -> Destinations.SignInFlow.route
        else -> Destinations.SignInFlow.route
    }

    handleDynamicLinks(navController)
    NavHost(navController, initialRoute) {
        navigation(
            route = Destinations.SignInFlow.route,
            startDestination = when {
                !(appState[AppState.OnBoarded] ?: false) -> Destinations.OnBoarding.route
                else -> Destinations.SignIn.route
            }
        ) {
            composable(Destinations.OnBoarding.route) { OnBoardingScreen(navController) }
            composable(Destinations.SignIn.route) { SignInScreen(navController) }
            composable(Destinations.SignUp.route) { /*SignUpScreen(navController)*/ }
            composable(Destinations.Forgot.route) { /*ForgotScreen(navController)*/ }
        }

        composable(Destinations.CalendarView.route) { }

        composable(
            "FirebaseAction?mode={mode}&oobCode={oobCode}&apiKey={apiKey}&continueUrl={continueUrl}&lang={lang}",
            deepLinks = listOf(
                NavDeepLink("$MultitaskerBaseUrl/user/action?mode={mode}&oobCode={oobCode}&apiKey={apiKey}&continueUrl={continueUrl}&lang={lang}"),
            ),
            arguments = listOf(
                navArgument("mode")        { },
                navArgument("oobCode")     { nullable = true },
                navArgument("apiKey")      { nullable = true },
                navArgument("lang")        { nullable = true },
                navArgument("continueUrl") {
                    defaultValue = null
                    nullable     = true
                },
            )
        ) {
            it.arguments?.let { bundle ->
                val mode:        String by bundle
                val oobCode:     String by bundle
                val apiKey:      String by bundle
                val continueUrl: String by bundle
                val lang:        String by bundle

                // Where ever we end up we only need the oobCode and continueUrl
                fun buildUrl(path: String) =
                    Uri.parse("https://$MultitaskerBaseUrl/user/$path?code=$oobCode&continueUrl=$continueUrl")

                navController.navigate(when(mode) {
                    "resetPassword" -> buildUrl("reset")
                    "recoverEmail"  -> buildUrl("recover")
                    "verifyEmail"   -> buildUrl("verify")
                    else -> throw IllegalArgumentException("Unknown firebase action mode given: $mode")
                }, navOptions {
                    popUpTo(it.destination.id) { // Remove this from the backstack
                        inclusive = true
                    }
                })
            }
        }
    }
}
