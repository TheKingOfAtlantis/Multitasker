package uk.co.sksulai.multitasker.ui.screen.signin

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import uk.co.sksulai.multitasker.R
import uk.co.sksulai.multitasker.db.viewmodel.UserViewModel
import uk.co.sksulai.multitasker.ui.component.ErrorText
import uk.co.sksulai.multitasker.util.provideInScope
import uk.co.sksulai.multitasker.util.rememberMutableState

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
) {
    val scope = rememberCoroutineScope()
    var email by rememberMutableState(initialEmail)
    var submitted by rememberMutableState(false)

    
    if(!submitted) {
        var error by rememberMutableState("")

        val onSubmitted = provideInScope(scope) {
            error = if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                "Not a valid email address" else ""

            if(error.isEmpty()) {
                userViewModel.resetPassword.request(email, "calendar")
                submitted = true
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
