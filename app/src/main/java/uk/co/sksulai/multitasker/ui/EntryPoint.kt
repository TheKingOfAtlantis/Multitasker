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

const val baseUrl = "app.multitasker.xyz"

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
            composable(
                "Forgot?email={email}&submitted={submitted}",
                arguments = listOf(
                    navArgument("email")     { defaultValue = "" },
                    navArgument("submitted") { defaultValue = false }
                )
            ) {
                it.arguments?.let { bundle ->
                    val email:     String  by bundle
                    val submitted: Boolean by bundle
                    Crossfade(targetState = submitted) {
                        ForgotPasswordScreen(
                            navController,
                            email     = rememberFieldState(email),
                            submitted = it
                        )
                    }
                }
            }
        }
        composable(
            "Action?mode={mode}&oobCode={oobCode}&apiKey={apiKey}&continueUrl={continueUrl}&lang={lang}",
            deepLinks = listOf(
                navDeepLink { uriPattern = "$baseUrl/user/action?mode={mode}&oobCode={oobCode}&apiKey={apiKey}&continueUrl={continueUrl}&lang={lang}" },
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

                when(mode) {
                    "resetPassword" -> ForgotPasswordScreen(navController, oobCode, continueUrl)
                    "recoverEmail"  -> TODO()
                    "verifyEmail"   -> TODO()
                }
            }
        }
    }
}
