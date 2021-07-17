package uk.co.sksulai.multitasker.ui.userFlow

import android.util.Log
import android.content.IntentSender
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.autofill.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.analytics.ktx.*
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.launch

import uk.co.sksulai.multitasker.R
import uk.co.sksulai.multitasker.db.repo.GoogleIntent
import uk.co.sksulai.multitasker.db.viewmodel.GoogleIntentLauncher
import uk.co.sksulai.multitasker.db.viewmodel.UserViewModel


@OptIn(ExperimentalComposeUiApi::class)
@Composable fun SignInScreen(
    navController: NavHostController,
    userViewModel: UserViewModel = viewModel(),
    email: FieldState    = rememberFieldState(),
    password: FieldState = rememberFieldState()
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val autofill           = LocalAutofill.current
    val autofillTree       = LocalAutofillTree.current

    val scaffoldState = rememberScaffoldState()
    val scope         = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState
    ){
        // We allow the user two cancellations before limiting its usage
        // May use the first cancellation to open ui when we first navigate to this screen
        var limitOneTap by rememberSaveable { mutableStateOf(2) }
        val googleSignInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                result -> scope.launch {
            try {
                userViewModel.authenticate(GoogleIntent(result.data))
                navController.navigate("Agenda")
            } catch (e: ApiException) {
                when(e.statusCode) {
                    CommonStatusCodes.CANCELED -> {
                        Log.d("Sign in", "User cancelled sign in w/ One-Tap")
                        limitOneTap--
                    }
                    else -> Log.d("Sign in", "Failed to sign in w/ One-Tap", e)
                }
            }
        } }
        val googleSignUpLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                result -> scope.launch {
            try {
                userViewModel.authenticate(GoogleIntent(result.data))
                navController.navigate("SignUp")
            } catch (e: ApiException) {
                when(e.statusCode) {
                    CommonStatusCodes.CANCELED -> {
                        Log.d("Sign up", "User cancelled sign up w/ One-Tap")
                        limitOneTap--
                    }
                    else -> Log.d("Sign up", "Failed to sign up w/ One-Tap", e)
                }
            }
        } }
        val googlePasswordSaverLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                result -> scope.launch {
        } }

        val (emailFocuser, passwordFocuser) = FocusRequester.createRefs()

        fun signInAction(
            email: FieldState, password: FieldState
        ) {
            keyboardController?.hide()
            scope.launch {
                email.error    = ""
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
                    userViewModel.authenticate(
                        email.text, password.text,
                        GoogleIntentLauncher(googlePasswordSaverLauncher)
                    )
                    navController.navigate("CalendarView") {
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
        }

        Box(
            Modifier.fillMaxSize(),
            Alignment.Center
        ) { ConstraintLayout {
            val (
                emailField,
                emailErrorText,
                passwordField,
                passwordErrorText,
                signInButton,
                signUpButton,
                forgotButton,
                divider,
                googleButton,
            ) = createRefs()

            val emailAutofillNode = AutofillNode(
                onFill = { email.text = it },
                autofillTypes = listOf(
                    AutofillType.EmailAddress,
                    AutofillType.Username
                )
            )
            autofillTree += emailAutofillNode

            TextField(
                modifier = Modifier.constrainAs(emailField) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .focusRequester(emailFocuser)
                    .onGloballyPositioned { emailAutofillNode.boundingBox = it.boundsInWindow() }
                    .onFocusChanged {
                        autofill?.apply {
                            if (it.isFocused) requestAutofillForNode(emailAutofillNode)
                            else cancelAutofillForNode(emailAutofillNode)
                        }
                    },
                value = email.text,
                onValueChange = { email.text = it },
                label   = { Text("Email") },
                isError = email.valid,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions { passwordFocuser.requestFocus() }
            )
            if(!email.valid) Text(
                modifier = Modifier.constrainAs(emailErrorText) {
                    top.linkTo(emailField.bottom, 8.dp)
                    start.linkTo(emailField.start)
                    end.linkTo(emailField.end)
                },
                text = email.error,
                color = MaterialTheme.colors.error
            )

            val passwordAutofillNode = AutofillNode(
                autofillTypes = listOf(AutofillType.Password, AutofillType.NewPassword),
                onFill = {
                    password.text = it
                    signInAction(email, password)
                }
            )
            autofillTree += passwordAutofillNode

            TextField(
                modifier = Modifier
                    .constrainAs(passwordField) {
                        if (!email.valid)
                            top.linkTo(emailErrorText.bottom, 8.dp)
                        else top.linkTo(emailField.bottom, 8.dp)
                        start.linkTo(emailField.start)
                    }
                    .focusRequester(passwordFocuser)
                    .onGloballyPositioned { passwordAutofillNode.boundingBox = it.boundsInWindow() }
                    .onFocusChanged {
                        autofill?.apply {
                            if (it.isFocused) requestAutofillForNode(passwordAutofillNode)
                            else cancelAutofillForNode(passwordAutofillNode)
                        }
                    },
                value = password.text,
                onValueChange = { password.text = it },
                label = { Text("Password") },
                trailingIcon = { IconToggleButton(checked = password.visible, onCheckedChange = { password.visible = it }) {
                    if(password.visible) Icon(Icons.Filled.Visibility, "Password visible")
                    else Icon(Icons.Filled.VisibilityOff, "Password not visible")
                }},
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions {
                    keyboardController?.hide()
                    signInAction(email, password)
                },
                visualTransformation = when {
                    password.visible -> VisualTransformation.None
                    else -> PasswordVisualTransformation()
                },
                isError = !password.valid
            )

            if(password.valid) Text(
                modifier = Modifier.constrainAs(passwordErrorText) {
                    top.linkTo(passwordField.bottom, 8.dp)
                    start.linkTo(passwordField.start)
                    end.linkTo(passwordField.end)
                },
                text = password.error,
                color = MaterialTheme.colors.error
            )

            Button(
                modifier = Modifier.constrainAs(signInButton) {
                    if(!password.valid)
                        top.linkTo(passwordErrorText.bottom, 8.dp)
                    else top.linkTo(passwordField.bottom, 8.dp)
                    start.linkTo(passwordField.start, 8.dp)
                    end.linkTo(signUpButton.start, 4.dp)

                    width = Dimension.fillToConstraints
                },
                onClick = { signInAction(email, password) }
            ) { Text("Sign In") }
            Button(
                modifier = Modifier.constrainAs(signUpButton) {
                    start.linkTo(signInButton.end, 4.dp)
                    end.linkTo(passwordField.end, 8.dp)
                    top.linkTo(signInButton.top)

                    width = Dimension.fillToConstraints
                },
                onClick = {
                    
                }
            ) { Text("Sign Up") }

            TextButton(
                modifier = Modifier.constrainAs(forgotButton) {
                    start.linkTo(signInButton.start)
                    end.linkTo(signUpButton.end)
                    top.linkTo(signInButton.bottom, 8.dp)

                    width = Dimension.fillToConstraints
                },
                onClick = {

            }
            ) { Text("Forgot Password") }

            Divider(Modifier.constrainAs(divider) {
                top.linkTo(forgotButton.bottom, 8.dp)
                start.linkTo(passwordField.start)
                end.linkTo(passwordField.end)

                width = Dimension.fillToConstraints
            })

            Button(modifier = Modifier.constrainAs(googleButton) {
                top.linkTo(divider.bottom, 16.dp)
                start.linkTo(passwordField.start, 8.dp)
                end.linkTo(passwordField.end, 8.dp)

                width = Dimension.fillToConstraints
            }, onClick = { scope.launch {
                try {
                    Log.d("Sign In", "Current limit count: $limitOneTap")
                    if(limitOneTap > 0)
                        userViewModel.authenticate(GoogleIntentLauncher(googleSignInLauncher))
                    else scaffoldState.snackbarHostState.showSnackbar(
                        "Apologises, usage of Google sign in has been limited"
                    )
                } catch(e: IntentSender.SendIntentException) {
                    Log.e("Sign in", "Failed to start One Tap UI", e)
                } catch(e: ApiException) {
                    Log.d("Sign in", "No saved credentials found", e)
                    userViewModel.create(GoogleIntentLauncher(googleSignUpLauncher))
                }
            }}) {
                Icon(
                    painterResource(R.drawable.ic_google_g_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = ButtonDefaults.IconSpacing)
                        .size(ButtonDefaults.IconSize),
                    tint = Color.Unspecified
                )
                Text("Continue with Google")
            }
        }
    } }
}
