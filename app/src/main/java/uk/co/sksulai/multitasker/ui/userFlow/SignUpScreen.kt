package uk.co.sksulai.multitasker.ui.userFlow

import kotlinx.coroutines.launch

import android.util.Log

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuthException

import uk.co.sksulai.multitasker.db.viewmodel.*

@ExperimentalComposeUiApi
@Composable fun SignUpScreen(
    navController: NavHostController,
    userViewModel: UserViewModel = viewModel(),
    email: FieldState    = rememberFieldState(),
    password: FieldState = rememberFieldState()
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scaffoldState      = rememberScaffoldState()
    val scope              = rememberCoroutineScope()

    val (emailFocuser, passwordFocuser) = FocusRequester.createRefs()
    val googlePasswordSaverLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            result -> scope.launch {
    } }

    Scaffold(
        Modifier.fillMaxSize(),
        scaffoldState
    ) { Column(
        Modifier.fillMaxSize(),
        Arrangement.Center,
        Alignment.CenterHorizontally
    ) {
        TextField(
            value = email.text,
            onValueChange = { email.text = it },
            label   = { Text("Email") },
            isError = !email.valid
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password.text,
            onValueChange = { password.text = it },
            label = { Text("Password") },
            visualTransformation = { TransformedText(
                text = AnnotatedString("*".repeat(it.text.length)),
                offsetMapping = OffsetMapping.Identity
            ) },
            isError = !password.valid
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = {
                keyboardController?.hide()
                scope.launch {
                    email.error = ""
                    password.error = ""
                    when {
                        email.text.isEmpty() -> {
                            email.error = "No email provided"
                            emailFocuser.requestFocus()
                            return@launch
                        }
                        password.text.isEmpty() -> {
                            password.error = "No password provided"
                            passwordFocuser.requestFocus()
                            return@launch
                        }
                    }
                    try {
                        userViewModel.create(
                            email.text, password.text,
                            GoogleIntentLauncher(googlePasswordSaverLauncher)
                        )
                        navController.navigate("signup/onboarding") {
                            popUpTo(navController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    } catch (e: FirebaseAuthException) {
                        userViewModel.handleAuthError(
                            err = e,
                            onEmailError = {
                                email.error = it
                                emailFocuser.requestFocus()
                            },
                            onPasswordError = {
                                password.error = it
                                passwordFocuser.requestFocus()
                            },
                            onAuthError = { scaffoldState.snackbarHostState.showSnackbar(it) }
                        )
                    } catch (e: ApiException) {
                        // If the saver fails isn't critical
                        // May fail if we just used autofill to get details
                        Log.e("Sign in/up", "Failed to save the email/password", e)
                    } finally {
                        Log.d("", "We should eventually arrive here")
                    }
                }
            }) { Text("Sign Up") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                navController.popBackStack()
                navController.navigate("SignIn" + when {
                    email.valid    -> "?email=${email.text}"
                    password.valid -> "${if(email.text.isEmpty()) "?" else "&"}password=${password.text}"
                    else -> ""
                }) { popUpTo("SignIn") { inclusive = true } }
            }) { Text("Back") }
        }
    } }
}
