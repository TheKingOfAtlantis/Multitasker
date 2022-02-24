package uk.co.sksulai.multitasker.ui.screen.signin

import kotlinx.coroutines.delay

import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import androidx.navigation.NavController
import androidx.navigation.navOptions
import androidx.hilt.navigation.compose.hiltViewModel

import com.google.firebase.auth.FirebaseAuthException

import uk.co.sksulai.multitasker.db.viewmodel.UserViewModel
import uk.co.sksulai.multitasker.util.rememberSaveableMutableState

@Composable fun EmailVerification(
    onSubmitRequest: () -> Unit,
    onCancelRequest: (() -> Unit)? = null,
    preamble: @Composable (() -> Unit)? = null,
) = Column(
    Modifier
        .fillMaxSize()
        .wrapContentSize(),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(
        "Email verification",
        style = MaterialTheme.typography.h6
    )

    preamble?.let {
        Box(Modifier.padding(
            horizontal = 24.dp,
            vertical = 16.dp
        )) {
            preamble()
        }
    }
    Button(onClick = onSubmitRequest) { Text("Send Request") }
    if(onCancelRequest != null)
        TextButton(onClick = onCancelRequest) { Text("Verify later") }
}

@Composable fun EmailVerification(
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
    val emailVerification = userViewModel.emailVerification

    var state by rememberSaveableMutableState(EmailActionState.Verifying)
    var error by rememberSaveableMutableState<String?>(null)

    when(state) {
        EmailActionState.Verifying -> {
            LaunchedEffect(Unit) {
                try {
                    emailVerification.confirm(code) // If it throws then it failed otherwise success
                    state = EmailActionState.Success
                } catch(e: FirebaseAuthException) {
                    error = when(e.errorCode) {
                        "ERROR_EXPIRED_ACTION_CODE"  -> "The verification link has expired."
                        "ERROR_INVALID_ACTION_CODE"  -> "Invalid verification link - Either malformed or already used."
                        "ERROR_USER_DISABLED"        -> "The user account has been disabled."
                        "ERROR_USER_NOT_FOUND"       -> "The user may have been deleted."
                        else -> "An unknown error occurred: ${e.errorCode} - ${e.localizedMessage}"
                    }
                    state = EmailActionState.Error
                }
            }

            Text("Verifying link...")
            CircularProgressIndicator(
                Modifier
                    .padding(8.dp)
                    .size(96.dp)
            )
        }
        EmailActionState.Success -> {
            Text(
                "Email verified",
                style = MaterialTheme.typography.h6,
            )

            LaunchedEffect(Unit) {
                delay(2000 /* 2s */)
                navController.navigate(Uri.parse(continueUrl), navOptions {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                })
            }
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
                onClick = { state = EmailActionState.Retry },
                content = { Text("Retry") }
            )
        }
        EmailActionState.Retry -> {
            Text(
                "Email verification",
                style = MaterialTheme.typography.h6
            )

            Text(
                text = "New request has been sent your email",
                modifier = Modifier.padding(
                    vertical = 8.dp,
                    horizontal = 24.dp
                ),
                textAlign = TextAlign.Center
            )

            LaunchedEffect(Unit) {
                // Resend the continueUrl (but we need to trim the domain first)
                emailVerification.request(continueUrl.removePrefix("https://app.multitasker.xyz/"))
            }
        }
    }
} }
