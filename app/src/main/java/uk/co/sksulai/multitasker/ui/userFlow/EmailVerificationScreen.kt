package uk.co.sksulai.multitasker.ui.userFlow

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester

import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import kotlinx.coroutines.delay
import uk.co.sksulai.multitasker.db.viewmodel.UserViewModel
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

enum class EmailVerificationState {
    Invalid,
    Verifying,
    Verified
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalTime::class)
@Composable fun EmailVerificationScreen(
    navController: NavHostController,
    code: String,
    continueUrl: String?,
) = Scaffold {
    val scope = rememberCoroutineScope()

    val userViewModel = viewModel<UserViewModel>()
    var state by remember { mutableStateOf(EmailVerificationState.Verifying) }

    Crossfade(targetState = state) {
        Column(
            Modifier.fillMaxSize(),
            Arrangement.Center,
            Alignment.CenterHorizontally
        ) {
            when (it) {
                EmailVerificationState.Invalid -> {
                    Text("Appears that link is no longer valid or malformed")
                    Text("Click below to try again")
                    Button(
                        modifier = Modifier.padding(top = 8.dp),
                        onClick = {
                            val navOption = NavOptions.Builder().apply {
                                setPopUpTo(navController.graph.findStartDestination().id, true)
                                setLaunchSingleTop(true)
                            }
                            navController.navigate(
                                Uri.parse("https://app.multitasker.xyz/user/signup"),
                                navOption.build()
                            )
                        }) { Text("Try again") }
                }
                EmailVerificationState.Verifying -> {
                    Text("Verifying the link")
                    Spacer(Modifier.height(8.dp))
                    CircularProgressIndicator()

                    LaunchedEffect(Unit) {
                        try {
                            userViewModel.emailVerification.confirm(code)
                            state = EmailVerificationState.Verified
                        } catch (e: Exception) {
                            state = EmailVerificationState.Invalid
                            Log.e("Email Verification", "Verification of the link failed", e)
                        }
                    }
                }
                EmailVerificationState.Verified -> {
                    Text("Your email has been verified")
                    LaunchedEffect(Unit) {
                        delay(Duration.seconds(3))
                        navController.navigate(
                            continueUrl!!.toUri()
                        )
                    }
                }
            }
        }
    }
}
