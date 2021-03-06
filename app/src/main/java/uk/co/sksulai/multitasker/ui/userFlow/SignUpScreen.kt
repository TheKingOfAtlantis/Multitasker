package uk.co.sksulai.multitasker.ui.userFlow

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp

import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.compose.navigate
import androidx.navigation.compose.popUpTo

import kotlinx.coroutines.launch

import uk.co.sksulai.multitasker.db.viewmodel.UserViewModel
import uk.co.sksulai.multitasker.util.LocalNavController

@Composable fun SignUpScreen(
    emailInitial: String,
    passwordInitial: String = ""
) {
    val navController = LocalNavController.current

    val userViewModel = viewModel<UserViewModel>()
    val scaffoldState = rememberScaffoldState()
    val scope         = rememberCoroutineScope()

    Scaffold(
        Modifier.fillMaxSize(),
        scaffoldState
    ) { Column(
        Modifier.fillMaxSize(),
        Arrangement.Center,
        Alignment.CenterHorizontally
    ) {
        var email    by rememberSaveable { mutableStateOf(emailInitial) }
        var password by rememberSaveable { mutableStateOf(passwordInitial) }

        var emailError    by rememberSaveable { mutableStateOf("") }
        var passwordError by rememberSaveable { mutableStateOf("") }

        TextField(
            value = email,
            onValueChange = { email = it },
            label   = { Text("Email") },
            isError = emailError.isNotEmpty()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = { TransformedText(
                text = AnnotatedString("*".repeat(it.text.length)),
                offsetMapping = OffsetMapping.Identity
            ) },
            isError = passwordError.isNotEmpty()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = { scope.launch {
                userViewModel.create(email, password) { emailErr, passwordErr, authErr ->
                    emailError    = emailErr
                    passwordError = passwordErr

                    if(authErr.isNotEmpty())
                        scaffoldState.snackbarHostState.showSnackbar(authErr)
                }
            }
            }) { Text("Sign In") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                navController.popBackStack()
                navController.navigate("SignIn" + when {
                    email.isNotEmpty()    -> "?email=${email}"
                    password.isNotEmpty() -> "${if(email.isEmpty()) "?" else "&"}password=${password}"
                    else -> ""
                }) { popUpTo("SignIn") { inclusive = true } }
            }) { Text("Back") }
        }
    } }
}
