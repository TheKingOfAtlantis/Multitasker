package uk.co.sksulai.multitasker.ui.userFlow

import android.content.IntentSender
import android.util.Log
import android.view.View
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.*
import androidx.compose.ui.autofill.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp

import androidx.constraintlayout.compose.*
import androidx.core.content.getSystemService
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

import com.google.android.gms.common.api.*
import com.google.firebase.auth.FirebaseAuthException

import kotlinx.coroutines.launch

import uk.co.sksulai.multitasker.R
import uk.co.sksulai.multitasker.db.repo.GoogleIntent
import uk.co.sksulai.multitasker.db.viewmodel.GoogleIntentLauncher
import uk.co.sksulai.multitasker.db.viewmodel.UserViewModel
import uk.co.sksulai.multitasker.util.setScreen

typealias EmailActionType = (email: FieldState, password: FieldState) -> Unit

@OptIn(ExperimentalComposeUiApi::class)
fun Autofill.getManager(view: View) = view.context.getSystemService<AutofillManager>()
@OptIn(ExperimentalComposeUiApi::class)
fun Autofill.onValueChanged(
    view: View, // = LocalView.current
    node: AutofillNode,
    value: String
) = getManager(view)?.notifyValueChanged(view, node.id, AutofillValue.forText(value))

@OptIn(ExperimentalComposeUiApi::class)
@Composable fun SignInScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel(),
    email: FieldState    = rememberFieldState(),
    password: FieldState = rememberFieldState(),
    view: View = LocalView.current
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val autofill           = LocalAutofill.current
    val autofillTree       = LocalAutofillTree.current

    val scaffoldState = rememberScaffoldState()
    val scope         = rememberCoroutineScope()

    val (emailFocuser, passwordFocuser) = FocusRequester.createRefs()

    // We allow the user two cancellations before limiting its usage
    // May use the first cancellation to open ui when we first navigate to this screen
    var limitOneTap by rememberSaveable { mutableStateOf(2) }

    val googleLauncher = @Composable { tag: String, destination: String ->
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { scope.launch {
            try {
                userViewModel.authenticate(GoogleIntent(it.data))
                navController.navigate(destination) {
                    navController.navigate("SignUp") {
                        popUpTo(navController.graph.findStartDestination().id)
                        launchSingleTop = true
                    }
                }
            } catch (e: ApiException) {
                when(e.statusCode) {
                    CommonStatusCodes.CANCELED -> {
                        Log.d(tag, "User cancelled sign in w/ One-Tap")
                        limitOneTap--
                    }
                    else -> Log.d(tag, "Failed to sign in w/ One-Tap", e)
                }
            }
        } }
    }
    val googleSignInLauncher = googleLauncher("Sign in", "CalendarView")
    val googleSignUpLauncher = googleLauncher("Sign up", "SignUp")
    val googlePasswordSaverLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        Log.d("Sign in", "We've launched the saver")
    }


    fun action(
        action: suspend (email: String, password: String, launcher: GoogleIntentLauncher) -> Unit,
        email: FieldState, password: FieldState,
        route: String
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
                action(
                    email.text, password.text,
                    GoogleIntentLauncher(googlePasswordSaverLauncher)
                )
                navController.navigate(route) {
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id)
                        launchSingleTop = true
                    }
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

    val signUpAction: EmailActionType = { email, password -> action(userViewModel::create, email, password, "SignUp") }
    val signInAction: EmailActionType = { email, password -> action(userViewModel::authenticate, email, password, "CalendarView") }


    val emailAutofillNode = AutofillNode(
        onFill = { email.text = it },
        autofillTypes = listOf(
            AutofillType.Username,
            AutofillType.EmailAddress
        )
    )
    autofillTree += emailAutofillNode

    val passwordAutofillNode = AutofillNode(
        autofillTypes = listOf(
            AutofillType.Password,
        ),
        onFill = { password.text = it }
    )
    autofillTree += passwordAutofillNode

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState
    ){
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
                onValueChange = {
                    email.text = it
                    autofill?.onValueChanged(view, emailAutofillNode, it)
                },
                label            = { Text("Email") },
                isError          = !email.valid,
                keyboardOptions  = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next
                ),
                keyboardActions  = KeyboardActions { passwordFocuser.requestFocus() }
            )
            if(!email.valid) Text(
                modifier = Modifier.constrainAs(emailErrorText) {
                    top.linkTo(emailField.bottom, 8.dp)
                    start.linkTo(emailField.start)
                    end.linkTo(emailField.end)
                    width = Dimension.fillToConstraints
                },
                text  = email.error,
                color = MaterialTheme.colors.error
            )

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
                onValueChange = {
                    password.text = it
                    autofill?.onValueChanged(view, passwordAutofillNode, it)
                },
                label        = { Text("Password") },
                isError      = !password.valid,
                trailingIcon = { IconToggleButton(password.visible, { password.visible = it }) {
                    if(password.visible) Icon(Icons.Filled.Visibility, "Password visible")
                    else Icon(Icons.Filled.VisibilityOff, "Password not visible")
                }},
                keyboardOptions  = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions  = KeyboardActions {
                    keyboardController?.hide()
                    signInAction(email, password)
                },
                visualTransformation = when {
                    password.visible -> VisualTransformation.None
                    else -> PasswordVisualTransformation()
                }
            )

            if(!password.valid) Text(
                modifier = Modifier.constrainAs(passwordErrorText) {
                    top.linkTo(passwordField.bottom, 8.dp)
                    start.linkTo(passwordField.start)
                    end.linkTo(passwordField.end)
                    width = Dimension.fillToConstraints
                },
                text  = password.error,
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
                onClick = { signUpAction(email, password) }
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
