package uk.co.sksulai.multitasker.ui

import java.lang.IllegalArgumentException

import android.net.Uri
import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences

import androidx.navigation.*
import androidx.navigation.compose.*

import com.google.firebase.ktx.Firebase
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks

import uk.co.sksulai.multitasker.ui.screen.signin.*
import uk.co.sksulai.multitasker.util.*
import uk.co.sksulai.multitasker.util.DatastoreLocators.AppState

const val MultitaskerBaseUrl = "app.multitasker.xyz"

enum class GraphLevel {
    Root,
    SignInFlow
}

@Composable fun rememberInitialRoute(
    level: GraphLevel,
    appState: Preferences
) = rememberSaveable(
    level, appState
) { when(level) {
    GraphLevel.Root -> when {
        !appState[AppState.CurrentUser].isNullOrEmpty() -> Destinations.CalendarView.route
        !(appState[AppState.OnBoarded] ?: false) -> Destinations.SignInFlow.route
        else -> Destinations.SignInFlow.route
    }
    GraphLevel.SignInFlow ->  when {
        !(appState[AppState.OnBoarded] ?: false) -> Destinations.OnBoarding.route
        else -> Destinations.SignIn.route
    }
} }

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
    val appState by AppState.retrieveData(emptyPreferences())

    val rootInitialRoot = rememberInitialRoute(GraphLevel.Root, appState)
    val signinFlowInitialRoot = rememberInitialRoute(GraphLevel.SignInFlow, appState)

    handleDynamicLinks(navController)
    NavHost(navController, rootInitialRoot) {
        navigation(
            route = Destinations.SignInFlow.route,
            startDestination = signinFlowInitialRoot
        ) {
            composable(Destinations.OnBoarding.route) { OnBoardingScreen(navController) }
            composable(
                Destinations.SignIn.route,
                deepLinks = listOf(NavDeepLink("$MultitaskerBaseUrl/signin"))
            ) { SignInScreen(navController) }
            composable(
                Destinations.SignUp.route,
                deepLinks = listOf(NavDeepLink("$MultitaskerBaseUrl/signup"))
            ) { SignUpScreen(navController) }
            composable(
                Destinations.Forgot.route + "?email={email}",
            ) {
                it.arguments?.let { args ->
                    val email: String? by args
                    ResetPassword(navController, email ?: "")
                }
            }

            fun emailActionComposable(
                route: String,
                deepLink: String,
                content: @Composable (code: String, continueUrl: String) -> Unit
            ) = composable(
                "$route?code={code}&continueUrl={continueUrl}",
                deepLinks = listOf(NavDeepLink("$deepLink?code={code}&continueUrl={continueUrl}")),
                arguments = listOf(
                    navArgument("code") { },
                    navArgument("continueUrl") { },
                )
            ) {
                it.arguments!!.let { bundle ->
                    val code:        String by bundle
                    val continueUrl: String by bundle

                    content(code, continueUrl)
                }
            }
            emailActionComposable(
                Destinations.Forgot.route,
                "$MultitaskerBaseUrl/user/reset"
            ) { code, continueUrl -> ResetPassword(navController, code, continueUrl)  }
        }

        composable(Destinations.CalendarView.route) { }

        composable(
            "Action?mode={mode}&oobCode={oobCode}&apiKey={apiKey}&continueUrl={continueUrl}&lang={lang}",
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
                // val apiKey:      String by bundle
                val continueUrl: String by bundle
                // val lang:        String by bundle

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
