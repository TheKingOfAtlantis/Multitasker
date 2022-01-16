package uk.co.sksulai.multitasker.ui.screen.signin

import androidx.compose.runtime.*

import androidx.compose.foundation.text.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.*
import androidx.compose.ui.res.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel

import uk.co.sksulai.multitasker.R
import uk.co.sksulai.multitasker.ui.component.*
import uk.co.sksulai.multitasker.db.viewmodel.UserViewModel
import uk.co.sksulai.multitasker.util.rememberMutableState

/**
 * Form containing fields for the user's email and password that can be used to
 * authenticate the user or any operation which requires the user's credentials
 *
 * @param email             The current email value
 * @param password          The current password value
 * @param onEmailChanged    On email changed callback
 * @param onPasswordChanged On password changed callback
 * @param emailError        Email related error message
 * @param passwordError     Password related error message
 * @param onFilled          On form filled callback
 * @param modifier          The modifier to apply to the form
 */
@ExperimentalComposeUiApi
@Composable fun SignInForm(
    email: String,
    password: String,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    emailError: String?,
    passwordError: String?,
    onFilled: (() -> Unit)?,
    modifier: Modifier = Modifier,
) = Column(modifier) {
    val (emailFocus, passwordFocus) = remember { FocusRequester.createRefs() }

    LaunchedEffect(emailError, passwordError) {
        if(!emailError.isNullOrEmpty()) emailFocus.requestFocus()
        else if(!passwordError.isNullOrEmpty()) passwordFocus.requestFocus()
    }

    OutlinedTextField(
        modifier = Modifier.focusOrder(emailFocus),
        label = {
            ErrorLabel(
                stringResource(id = R.string.email),
                !emailError.isNullOrEmpty()
            )
        },
        value         = email,
        onValueChange = onEmailChanged,
        singleLine    = true,
        isError       = !emailError.isNullOrEmpty(),
        keyboardActions  = KeyboardActions { passwordFocus.requestFocus() },
        keyboardOptions  = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction    = ImeAction.Next
        ),
    )
    ErrorText(
        emailError,
        modifier = Modifier
            .paddingFromBaseline(24.dp)
            .padding(start = 16.dp),
        textStyle = MaterialTheme.typography.caption
    )

    Spacer(Modifier.height(8.dp))

    var visible by rememberMutableState(false)
    OutlinedTextField(
        modifier = Modifier.focusRequester(passwordFocus),
        label    = {
            ErrorLabel(
                stringResource(id = R.string.password),
                !passwordError.isNullOrEmpty()
            )
        },
        value         = password,
        onValueChange = onPasswordChanged,
        singleLine    = true,
        isError       = !passwordError.isNullOrEmpty(),
        trailingIcon  = {
            IconToggleButton(checked = visible, onCheckedChange = { visible = it }) {
                Icon(
                    imageVector =
                        if(visible) Icons.Default.Visibility
                        else Icons.Default.VisibilityOff,
                    contentDescription = null
                )
            }
        },
        visualTransformation =
            if(visible) VisualTransformation.None
            else PasswordVisualTransformation(),
        keyboardOptions  = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction    = ImeAction.Done
        ),
        keyboardActions = KeyboardActions {
            passwordFocus.freeFocus()
            onFilled?.invoke()
        }
    )
    ErrorText(
        passwordError,
        modifier = Modifier
            .paddingFromBaseline(24.dp)
            .padding(start = 16.dp),
        textStyle = MaterialTheme.typography.caption
    )
}
@OptIn(ExperimentalComposeUiApi::class)
@Composable fun SignInScreen(
    navController: NavHostController,
    user: UserViewModel = hiltViewModel()
) {

}
