package uk.co.sksulai.multitasker.ui.screen.signin

import android.net.Uri
import android.util.Patterns
import androidx.activity.compose.BackHandler

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import androidx.navigation.NavController
import androidx.navigation.navOptions
import androidx.hilt.navigation.compose.hiltViewModel

import com.google.firebase.auth.FirebaseAuthException

import uk.co.sksulai.multitasker.R
import uk.co.sksulai.multitasker.db.viewmodel.UserViewModel
import uk.co.sksulai.multitasker.ui.Destinations
import uk.co.sksulai.multitasker.ui.component.ErrorText
import uk.co.sksulai.multitasker.util.provideInScope
import uk.co.sksulai.multitasker.util.rememberMutableState
import uk.co.sksulai.multitasker.util.rememberSaveableMutableState

/**
 * Password Reset Form
 *
 * @param navController Navigation controller to move back up
 * @param initialEmail  Initial email to prefill the form with (taken from sign in)
 * @param userViewModel Needed to send the request
 */
@Composable fun ResetPassword(
    navController: NavController,
    initialEmail: String,
    userViewModel: UserViewModel = hiltViewModel()
) = Scaffold(
    topBar = {
        TopAppBar(
            navigationIcon = {
                IconButton(
                    onClick = { navController.navigateUp() },
                    content = { Icon(Icons.Default.ArrowBack, null) }
                )
            },
            title = { },
            elevation = 0.dp
        )
    }
) { Column(
    Modifier
        .fillMaxSize()
        .wrapContentSize(),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    val scope = rememberCoroutineScope()
    var email by rememberMutableState(initialEmail)
    var submitted by rememberMutableState(false)

    BackHandler(enabled = submitted) { submitted = false }
    
    if(!submitted) {
        var error by rememberMutableState("")

        val onSubmitted = provideInScope(scope) {
            error = if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                "Not a valid email address" else ""

            if(error.isEmpty()) {
                try {
                    userViewModel.resetPassword.request(email, "calendar")
                    submitted = true
                } catch(e: FirebaseAuthException) {
                    if(e.errorCode == "ERROR_USER_NOT_FOUND")
                        error = "No account found with that email"
                    else throw e
                }
            }
        }

        Text(
            "Reset Password",
            style = MaterialTheme.typography.h6
        )
        Text(
            "Enter the email associated with your account and we'll send a link to it",
            modifier = Modifier.padding(
                vertical   = 16.dp,
                horizontal = 32.dp
            ),
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            label = { Text(stringResource(id = R.string.email)) },
            value = email,
            onValueChange    = { email = it },
            singleLine       = true,
            isError          = error.isNotEmpty(),
            keyboardActions  = KeyboardActions { onSubmitted() },
            keyboardOptions  = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction    = ImeAction.Done
            ),
        )
        ErrorText(
            error,
            Modifier
                .paddingFromBaseline(24.dp)
                .padding(start = 16.dp)
        )

        Row(Modifier.padding(16.dp)) {
            Button(
                onClick = onSubmitted,
                content = { Text("Submit") }
            )
            Spacer(Modifier.width(4.dp))
            TextButton(
                onClick = { navController.navigateUp() },
                content = { Text("Cancel") }
            )
        }
    } else {
        val censoredEmail = run {
            val domain   = email.substring(email.indexOfLast { it == '@' })
            val username = email.removeSuffix(domain)

            username.replaceRange(
                2, username.length - 1,
                "*".repeat(username.length - 3)
            ) + domain
        }

        Text(
            "Request submitted",
            style = MaterialTheme.typography.h6
        )
        Spacer(Modifier.height(16.dp))
        Text("Request sent to $censoredEmail...")
        Text("Double check your junk if you can't find it")
    }

} }

enum class EmailActionState {
    Verifying,
    Success,
    Error,
    Retry
}

@Composable fun ResetPassword(
    navController: NavController,
    code: String,
    continueUrl: String,
    userViewModel: UserViewModel = hiltViewModel()
) = Surface { Column(
    modifier = Modifier
        .fillMaxSize()
        .wrapContentHeight(),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    val scope = rememberCoroutineScope()

    val resetPassword = userViewModel.resetPassword

    var state by rememberSaveableMutableState(EmailActionState.Verifying)
    var error by rememberSaveableMutableState<String?>(null)
    var email by rememberSaveableMutableState("")

    @Suppress("NON_EXHAUSTIVE_WHEN_STATEMENT") // We don't need to retry here
    when(state) {
        EmailActionState.Verifying -> {
            LaunchedEffect(Unit) {
                try {
                    email = resetPassword.isValid(code)
                    state = EmailActionState.Success
                } catch(e: FirebaseAuthException) {
                    error = when(e.errorCode) {
                        "ERROR_EXPIRED_ACTION_CODE"  -> "The password reset link has expired."
                        "ERROR_INVALID_ACTION_CODE"  -> "Invalid password reset link - Either malformed or already used."
                        "ERROR_USER_DISABLED"        -> "The user account has been disabled."
                        "ERROR_USER_NOT_FOUND"       -> "The user may have been deleted."
                        else -> "An unknown error occurred: ${e.errorCode} - ${e.localizedMessage}"
                    }
                    state = EmailActionState.Error
                }
            }

            Text("Verifying link...")
            CircularProgressIndicator()
        }
        EmailActionState.Success -> {
            var password by rememberSaveableMutableState("")

            Text(
                "Link verified",
                style = MaterialTheme.typography.h6,
            )
            Text(
                "Update your password",
                modifier = Modifier.padding(vertical = 8.dp)
            )

            OutlinedTextField(
                label = { Text("New Password") },
                value = password,
                onValueChange = { password = it }
            )

            Button(
                modifier = Modifier.padding(8.dp),
                onClick = provideInScope(scope) {
                    resetPassword.reset(code, email, password)
                    navController.navigate(Uri.parse(continueUrl), navOptions {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    })
                },
                content = {Text("Submit") }
            )
        }
        EmailActionState.Error -> {
            Text(
                "Link invalid",
                style = MaterialTheme.typography.h6,
            )
            Text(
                error ?: "",
                modifier = Modifier.padding(
                    vertical = 8.dp,
                    horizontal = 24.dp
                ),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = provideInScope(scope) {
                    Destinations.SignIn.navigate(navController) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                    Destinations.Forgot.navigate(
                        navController,
                        mapOf("continueUrl" to continueUrl)
                    )
                },
                content = {Text("Retry") }
            )
        }
    }
} }
