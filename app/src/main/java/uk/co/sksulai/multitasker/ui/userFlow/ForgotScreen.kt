package uk.co.sksulai.multitasker.ui.userFlow

import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
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
import uk.co.sksulai.multitasker.db.viewmodel.UserViewModel

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
    val scope = rememberCoroutineScope()
    when(submitted) {
        false -> {
            val autofill     = LocalAutofill.current
            val autofillTree = LocalAutofillTree.current
            val autofillNode = AutofillNode(listOf(
                AutofillType.Username,
                AutofillType.EmailAddress
            ), onFill = { email.text = it })

            autofillTree += autofillNode
            val focus = FocusRequester.Default
            LaunchedEffect(Unit) { focus.requestFocus() }
            val resetEmail: () -> Unit = { scope.launch {
                userViewModel.resetPassword.request(email.text)
                navController.navigate("Forgot?email=${email.text}&submitted=true")
            } }

            Text("Enter the email associated with your account")
            Spacer(Modifier.height(8.dp))
            TextField(
                modifier = Modifier
                    .onGloballyPositioned {
                        autofillNode.boundingBox = it.boundsInParent()
                    }
                    .onFocusChanged {
                        autofill?.apply {
                            if (it.isFocused) requestAutofillForNode(autofillNode)
                            else cancelAutofillForNode(autofillNode)
                        }
                    }
                    .focusRequester(focus),
                value = email.text,
                onValueChange = { email.text = it },
                keyboardActions = KeyboardActions { resetEmail() },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Send
                )
            )
            Button(onClick = resetEmail) { Text("Send Request") }
        }
        true -> {
            Text("Reset request submitted")
            Text("Check your email for the reset link")
            Spacer(Modifier.height(16.dp))
            Button(onClick = { navController.navigateUp() }) { Text("Change email") }
        }
    }
}
}
