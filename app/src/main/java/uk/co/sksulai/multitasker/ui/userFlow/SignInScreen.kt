package uk.co.sksulai.multitasker.ui.userFlow

import android.util.Log
import android.content.IntentSender
import androidx.activity.compose.registerForActivityResult
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
import androidx.compose.ui.focus.isFocused
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

import androidx.navigation.compose.navigate
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.analytics.ktx.*
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

import uk.co.sksulai.multitasker.R
import uk.co.sksulai.multitasker.db.repo.GoogleIntent
import uk.co.sksulai.multitasker.db.viewmodel.GoogleIntentLauncher
import uk.co.sksulai.multitasker.db.viewmodel.UserViewModel
import uk.co.sksulai.multitasker.util.LocalNavController


@OptIn(ExperimentalComposeUiApi::class)
@Composable fun SignInScreen() {
    val navController = LocalNavController.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val autofill           = LocalAutofill.current
    val autofillTree       = LocalAutofillTree.current

    val userViewModel = viewModel<UserViewModel>()
    val scaffoldState = rememberScaffoldState()
    val scope         = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState
    ){
        // We allow the user two cancellations before limiting its usage
        // May use the first cancellation to open ui when we first navigate to this screen
        var limitOneTap by rememberSaveable { mutableStateOf(2) }
        val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
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
        val googleSignUpLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
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
        val googlePasswordSaverLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                result -> scope.launch {
        } }

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

            val (emailFocuser, passwordFocuser) = FocusRequester.createRefs()

            var email    by rememberSaveable { mutableStateOf("") }
            var password by rememberSaveable { mutableStateOf("") }

            var emailError      by rememberSaveable { mutableStateOf("") }
            var passwordError   by rememberSaveable { mutableStateOf("") }
            var passwordVisible by rememberSaveable { mutableStateOf(false) }

            val signInAction = { scope.launch { userViewModel.authenticate(
                email,
                password,
                GoogleIntentLauncher(googlePasswordSaverLauncher)
            ) { emailErr, passwordErr, authErr ->
                emailError    = emailErr
                passwordError = passwordErr

                if(authErr.isNotEmpty())
                    scaffoldState.snackbarHostState.showSnackbar(authErr)
            } } }

            val emailAutofillNode = AutofillNode(
                onFill = { email = it },
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
                value = email,
                onValueChange = { email = it },
                label   = { Text("Email") },
                isError = emailError.isNotEmpty(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions {
                    passwordFocuser.requestFocus()
                }
            )
            if(emailError.isNotEmpty()) Text(
                modifier = Modifier.constrainAs(emailErrorText) {
                    top.linkTo(emailField.bottom, 8.dp)
                    start.linkTo(emailField.start)
                    end.linkTo(emailField.end)
                },
                text = emailError,
                color = MaterialTheme.colors.error
            )

            val passwordAutofillNode = AutofillNode(
                autofillTypes = listOf(AutofillType.Password, AutofillType.NewPassword),
                onFill = {
                    password = it
                    signInAction()
                }
            )
            autofillTree += passwordAutofillNode

            TextField(
                modifier = Modifier
                    .constrainAs(passwordField) {
                        if (emailError.isNotEmpty())
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
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                trailingIcon = { IconToggleButton(checked = passwordVisible, onCheckedChange = { passwordVisible = it }) {
                    if(passwordVisible) Icon(Icons.Filled.Visibility, "Password visible")
                    else Icon(Icons.Filled.VisibilityOff, "Password not visible")
                }},
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions {
                    keyboardController?.hideSoftwareKeyboard()
                    signInAction()
                },
                visualTransformation = when {
                    passwordVisible -> VisualTransformation.None
                    else -> PasswordVisualTransformation()
                },
                isError = passwordError.isNotEmpty()
            )

            if(passwordError.isNotEmpty()) Text(
                modifier = Modifier.constrainAs(passwordErrorText) {
                    top.linkTo(passwordField.bottom, 8.dp)
                    start.linkTo(passwordField.start)
                    end.linkTo(passwordField.end)
                },
                text = passwordError,
                color = MaterialTheme.colors.error
            )

            Button(
                modifier = Modifier.constrainAs(signInButton) {
                    if(passwordError.isNotEmpty())
                        top.linkTo(passwordErrorText.bottom, 8.dp)
                    else top.linkTo(passwordField.bottom, 8.dp)
                    start.linkTo(passwordField.start, 8.dp)
                    end.linkTo(signUpButton.start, 4.dp)

                    width = Dimension.fillToConstraints
                },
                onClick = { signInAction() }
            ) { Text("Sign In") }
            Button(
                modifier = Modifier.constrainAs(signUpButton) {
                    start.linkTo(signInButton.end, 4.dp)
                    end.linkTo(passwordField.end, 8.dp)
                    top.linkTo(signInButton.top)

                    width = Dimension.fillToConstraints
                },
                onClick = { scope.launch { userViewModel.create(
                        email,
                        password,
                        GoogleIntentLauncher(googlePasswordSaverLauncher)
                    ) { emailErr, passwordErr, authErr ->
                        emailError = emailErr
                        passwordError = passwordErr

                        if (authErr.isNotEmpty())
                            scaffoldState.snackbarHostState.showSnackbar(authErr)
                    }
                }}) { Text("Sign Up") }

            TextButton(
                modifier = Modifier.constrainAs(forgotButton) {
                    start.linkTo(signInButton.start)
                    end.linkTo(signUpButton.end)
                    top.linkTo(signInButton.bottom, 8.dp)

                    width = Dimension.fillToConstraints
                },
                onClick = {

            }) { Text("Forgot Password") }

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
                        if(limitOneTap > 0)
                            userViewModel.authenticate(GoogleIntentLauncher(googleSignInLauncher))
                        else scaffoldState.snackbarHostState.showSnackbar(
                            "Apologises, usage of Google sign in has been limited"
                        )
                    } catch(e: IntentSender.SendIntentException) {
                        Log.e("Sign in", "Failed to start One Tap UI", e)
                    } catch(e: ApiException) {
                        when(e.statusCode) {
                            CommonStatusCodes.CANCELED -> {
                                Log.d("Sign up", "User cancelled sign up w/ One-Tap")
                                scaffoldState.snackbarHostState.showSnackbar(
                                    "Usage of Google sign in has been limited"
                                )
                                limitOneTap = 0
                            }
                            else -> {
                                Log.d("Sign in", "No saved credentials found", e)
                                userViewModel.create(GoogleIntentLauncher(googleSignUpLauncher))
                            }
                        }
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
