package uk.co.sksulai.multitasker.ui.userFlow

import kotlinx.coroutines.launch

import android.util.Log
import androidx.compose.animation.Crossfade

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

import androidx.compose.ui.*
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp

import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

import uk.co.sksulai.multitasker.db.viewmodel.UserViewModel
import uk.co.sksulai.multitasker.util.setScreen

@OptIn(ExperimentalComposeUiApi::class)
@Composable fun ForgotPasswordScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel(),
    email: FieldState = rememberFieldState(),
    submitted: Boolean = false
) = Scaffold { Column(
    Modifier.fillMaxSize(),
    Arrangement.Center,
    Alignment.CenterHorizontally
) {
    setScreen("Forgot Password")
    when(submitted) {
        false -> {
            val scope = rememberCoroutineScope()

            val autofill     = LocalAutofill.current
            val autofillTree = LocalAutofillTree.current
            val autofillNode = AutofillNode(listOf(
                AutofillType.Username,
                AutofillType.EmailAddress
            ), onFill = { email.text = it }).also { autofillTree += it }

            val focus = FocusRequester.Default

            LaunchedEffect(Unit) { focus.requestFocus() }

            val resetEmail: () -> Unit = { scope.launch {
                userViewModel.resetPassword.request(email.text)
                navController.navigate("Forgot?email=${email.text}&submitted=true")
            } }

            Text("Enter the email associated with your account")
            TextField(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .onGloballyPositioned { autofillNode.boundingBox = it.boundsInParent() }
                    .onFocusChanged {
                        autofill?.apply {
                            if (it.isFocused) requestAutofillForNode(autofillNode)
                            else cancelAutofillForNode(autofillNode)
                        }
                    }
                    .focusRequester(focus),
                value = email.text,
                onValueChange    = { email.text = it },
                keyboardActions  = KeyboardActions { resetEmail() },
                keyboardOptions  = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Send
                )
            )
            Button(
                modifier = Modifier.padding(16.dp),
                onClick = resetEmail
            ) { Text("Send Request") }
        }
        true -> {
            Text("Reset request submitted")
            Text("Check your email for the reset link")
            Spacer(Modifier.height(16.dp))
            Button(onClick = { navController.navigateUp() }) { Text("Change email") }
            Button(onClick = { navController.navigate("SignIn?$email") {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            } }) { Text("Back") }
        }
    }
} }

private enum class PasswordForgotState {
    Invalid,
    Verifying,
    Change
}
@OptIn(ExperimentalComposeUiApi::class)
@Composable fun ForgotPasswordScreen(
    navController: NavController,
    code: String,
    continueUrl: String?,
) = Scaffold {
    setScreen("Forgot Password")

    val scope = rememberCoroutineScope()

    val userViewModel = viewModel<UserViewModel>()
    var state by remember { mutableStateOf(PasswordForgotState.Verifying) }
    var email by remember { mutableStateOf("") }
    val passwordFocuser = FocusRequester.Default

    Crossfade(targetState = state) {
        Column(
            Modifier.fillMaxSize(),
            Arrangement.Center,
            Alignment.CenterHorizontally
        ) {
            when (it) {
                PasswordForgotState.Invalid -> {
                    Text("Appears that link is no longer valid or malformed")
                    Text("Click below to try again")
                    Button(
                        modifier = Modifier.padding(start = 8.dp),
                        onClick = {
                            navController.navigate("Forgot") {
                                popUpTo(navController.graph.findStartDestination().id)
                                launchSingleTop = true
                        }
                    }) { Text("Try again") }
                }
                PasswordForgotState.Verifying -> {
                    Text("Verifying the link")
                    Spacer(Modifier.height(8.dp))
                    CircularProgressIndicator()

                    LaunchedEffect(Unit) {
                        try {
                            email = userViewModel.resetPassword.isValid(code)
                            state = PasswordForgotState.Change
                        } catch (e: Exception) {
                            state = PasswordForgotState.Invalid
                            Log.e("Forgot", "Verification of the link failed", e)
                        }
                    }
                }
                PasswordForgotState.Change -> {
                    Text("Update your password")

                    val password = rememberFieldState()

                    val keyboardController = LocalSoftwareKeyboardController.current
                    val autofill     = LocalAutofill.current
                    val autofillTree = LocalAutofillTree.current

                    val reset: () -> Unit = {
                        scope.launch {
                            userViewModel.resetPassword.reset(code, email, password.text)
                        }
                    }

                    val passwordAutofillNode = AutofillNode(
                        autofillTypes = listOf(AutofillType.Password, AutofillType.NewPassword),
                        onFill        = { password.text = it }
                    )
                    autofillTree += passwordAutofillNode

                    TextField(
                        modifier = Modifier
                            .padding(16.dp)
                            .focusRequester(passwordFocuser)
                            .onGloballyPositioned { passwordAutofillNode.boundingBox = it.boundsInWindow() }
                            .onFocusChanged {
                                autofill?.apply {
                                    if (it.isFocused) requestAutofillForNode(passwordAutofillNode)
                                    else cancelAutofillForNode(passwordAutofillNode)
                                }
                            },
                        value         = password.text,
                        onValueChange = { password.text = it },
                        label         = { Text("Password") },
                        trailingIcon  = {
                            IconToggleButton(
                                checked         = password.visible,
                                onCheckedChange = { password.visible = it }) {
                                if(password.visible) Icon(Icons.Filled.Visibility, "Password visible")
                                else Icon(Icons.Filled.VisibilityOff, "Password not visible")
                            }
                        },
                        keyboardOptions  = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions {
                            keyboardController?.hide()
                            reset()
                        },
                        visualTransformation = when {
                            password.visible -> VisualTransformation.None
                            else -> PasswordVisualTransformation()
                        },
                        isError = !password.valid
                    )
                    if (!password.valid) Text(
                        text  = password.error,
                        color = MaterialTheme.colors.error
                    )

                    Button(
                        modifier = Modifier.padding(8.dp),
                        onClick  = reset
                    ) { Text("Submit") }
                }
            }
        }
    }
}
