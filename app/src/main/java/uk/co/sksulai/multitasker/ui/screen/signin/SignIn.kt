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

/**
 * Provides a series of buttons to handle operations involving email-based
 * credentials. While the naming is prejudiced towards handling operations
 * involved in the sign-in flow, the labels can be reassigned and buttons
 * will dynamically be added if the appropriate callback has been added.
 *
 * @param modifier Modifier to be applied to this component
 *
 * @param onSignIn Used to handle sign in requests
 * @param onSignUp Used to handle sign up requests. If marked as null the
 *                 button adjacent to the sign in button will be removed
 *                 and the sign in button will take up the remaining space
 * @param onForgot Used to handle forgotten password request. If marked
 *                 as null the button below the sign in (and sign up if
 *                 present) will be removed
 *
 * @param signInLabel The sign in button label (default: [R.string.signin])
 * @param signUpLabel The sign up button label (default: [R.string.signup])
 * @param forgotPasswordLabel The forgotten password button label (default:
 *                            [R.string.forgot_password])
 */
@Composable fun EmailActions(
    onSignIn: () -> Unit,
    onSignUp: (() -> Unit)? = null,
    onForgot: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    signInLabel: String = stringResource(R.string.signin),
    signUpLabel: String = stringResource(R.string.signup),
    forgotPasswordLabel: String = stringResource(R.string.forgot_password)
) = Column(modifier) {
    Row {
        Button(
            modifier = Modifier.weight(1f),
            onClick  = onSignIn,
            content  = { Text(signInLabel) }
        )
        if(onSignUp != null) {
            Spacer(Modifier.width(8.dp))
            Button(
                modifier = Modifier.weight(1f),
                onClick = onSignUp,
                content = { Text(signUpLabel) }
            )
        }
    }
    if(onForgot != null) {
        TextButton(
            modifier = Modifier.fillMaxWidth(),
            onClick  = onForgot,
            content  = { Text(forgotPasswordLabel) }
        )
    }
}
@OptIn(ExperimentalComposeUiApi::class)
@Composable fun SignInScreen(
    navController: NavHostController,
    user: UserViewModel = hiltViewModel()
) {

}
