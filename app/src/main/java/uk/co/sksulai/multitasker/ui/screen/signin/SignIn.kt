package uk.co.sksulai.multitasker.ui.screen.signin

import kotlin.math.max
import kotlinx.coroutines.launch

import android.util.Log

import android.view.View
import android.content.IntentSender
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.text.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.*
import androidx.compose.ui.res.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.core.content.getSystemService

import android.view.autofill.*
import androidx.compose.ui.autofill.*

import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuthException

import uk.co.sksulai.multitasker.R
import uk.co.sksulai.multitasker.ui.*
import uk.co.sksulai.multitasker.ui.component.*
import uk.co.sksulai.multitasker.db.repo.GoogleIntent
import uk.co.sksulai.multitasker.db.viewmodel.GoogleIntentLauncher
import uk.co.sksulai.multitasker.db.viewmodel.UserViewModel
import uk.co.sksulai.multitasker.util.provideInScope
import uk.co.sksulai.multitasker.util.rememberMutableState

@OptIn(ExperimentalComposeUiApi::class)
fun View.getManager() = context.getSystemService<AutofillManager>()
@OptIn(ExperimentalComposeUiApi::class)
fun AutofillNode.onValueChanged(
    view: View, // = LocalView.current
    value: String
) = view.getManager()?.notifyValueChanged(view, id, AutofillValue.forText(value))

@ExperimentalComposeUiApi
fun Modifier.addAutofillNode(
    autofillTree: AutofillTree,
    autofill: Autofill?,
    onFill: ((String) -> Unit)?,
    vararg type: AutofillType,
    onNodeCreated: (AutofillNode) -> Unit
): Modifier = this.composed {
    val node = remember(onFill, type) { AutofillNode(
        autofillTypes = type.asList(),
        onFill = onFill
    ) }
    autofillTree += node
    onNodeCreated(node)

    onGloballyPositioned {
        node.boundingBox = it.boundsInWindow()
    }.onFocusChanged {
        autofill?.apply {
            if (it.isFocused) requestAutofillForNode(node)
            else cancelAutofillForNode(node)
        }
    }
}

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

    val view         = LocalView.current
    val autofillTree = LocalAutofillTree.current
    val autofill     = LocalAutofill.current

    val (emailNode, onEmailNodeChange) = rememberMutableState<AutofillNode?>(null)
    OutlinedTextField(
        modifier = Modifier
            .focusOrder(emailFocus)
            .addAutofillNode(
                autofillTree, autofill, onEmailChanged,
                AutofillType.EmailAddress,
                onNodeCreated = onEmailNodeChange
            ),
        label = {
            ErrorLabel(
                stringResource(id = R.string.email),
                !emailError.isNullOrEmpty()
            )
        },
        value = email,
        onValueChange = {
            onEmailChanged(it)
            emailNode?.onValueChanged(view, it)
        },
        singleLine       = true,
        isError          = !emailError.isNullOrEmpty(),
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

    val (passwordNode, onPasswordNodeChange) = rememberMutableState<AutofillNode?>(null)
    var visible by rememberMutableState(false)
    OutlinedTextField(
        modifier = Modifier
            .focusRequester(passwordFocus)
            .addAutofillNode(
                autofillTree, autofill, onPasswordChanged,
                AutofillType.Password, AutofillType.NewPassword,
                onNodeCreated = onPasswordNodeChange
            )
        ,
        label = {
            ErrorLabel(
                stringResource(id = R.string.password),
                !passwordError.isNullOrEmpty()
            )
        },
        value = password,
        onValueChange = {
            onPasswordChanged(it)
            passwordNode?.onValueChanged(view, it)
        },
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

/**
 * Provides a list of buttons for each of the supported authentication methods.
 * It supports adding a preamble and modifying the button's text so that, if
 * required, additional context can be provided (such as the benefits of linking
 * a authentication provider) and that the buttons action can be specified (such
 * as linking or unlinking an account)
 *
 * @param modifier       Modifier to be applied to this component
 * @param onGoogle       Callback for when the user clicks the google button
 * @param googleText     The text to display in the google button
 * @param googlePreamble Text to be show before the google button that can be optionally
 *                       used to provide rational to the user (e.g. the benefit of
 *                       linking their google account)
 */
@Composable fun AuthProviders(
    modifier: Modifier = Modifier,
    onGoogle: () -> Unit,
    googleText: @Composable () -> Unit,
    googlePreamble: @Composable (() -> Unit)? = null
) = Column(modifier) {
    /**
     * Helper to provide the same layout for each authentication provider
     *
     * @param onClick The relevent onClick handler
     * @param icon The icon/logo associated with the authenticator's button
     * @param text The text to place on the button
     * @param preamble Preamble to place before the text
     */
    @Composable fun ProviderLayout(
        onClick: () -> Unit,
        icon: Painter,
        text: @Composable () -> Unit,
        preamble: @Composable (() -> Unit)? = null
    ) = Column {
        preamble?.let {
            Box(Modifier.padding(vertical = 4.dp)) {
                // TODO: Potentially pick a particular text style and content
                //       alpha (potentially set it to medium)
                preamble()
            }
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick  = onClick
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = ButtonDefaults.IconSpacing)
                    .size(ButtonDefaults.IconSize),
                tint = Color.Unspecified
            )
            text()
        }
    }

    ProviderLayout( // Google
        onClick  = onGoogle,
        icon     = painterResource(R.drawable.ic_google_g_logo),
        text     = googleText,
        preamble = googlePreamble
    )
}

/**
 * Layouts out the contents of the sign in form such that the width
 * of everything matches the text fields
 */
@Composable fun SignInScreenLayout(
    logo: @Composable () -> Unit,
    emailForm: @Composable () -> Unit,
    emailActions: @Composable () -> Unit,
    authProvider: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current

    Layout(
        modifier = modifier,
        content = {
            Box(Modifier.layoutId("logo")) { logo() }
            Box(Modifier.layoutId("emailForm")) { emailForm() }
            Box(Modifier.layoutId("emailActions")) { emailActions() }
            Box(Modifier.layoutId("authProvider")) { authProvider() }
        }
    ) { measurables, constraints ->
        val relax = constraints.copy(minWidth = 0, minHeight = 0)

        val header = measurables
            .first { it.layoutId == "logo" }
            .measure(relax)

        // Measure email form first so we can use its with to constrain everything else
        val emailFormPlaceable = measurables
            .first { it.layoutId == "emailForm" }
            .measure(relax)

        val emailActionsPlaceable = measurables
            .first { it.layoutId == "emailActions" }
            .measure(Constraints.fixedWidth(emailFormPlaceable.width))

        val authProviderPlaceable = measurables
            .first { it.layoutId == "authProvider" }
            .measure(Constraints.fixedWidth(emailFormPlaceable.width))

        val width = maxOf(
            constraints.minWidth,
            emailFormPlaceable.width,
            header.width,
            emailFormPlaceable.width,
            emailFormPlaceable.width,
            authProviderPlaceable.width,
        )
        val height = max(
            constraints.minHeight,
            header.height +
                emailFormPlaceable.height +
                emailFormPlaceable.height +
                authProviderPlaceable.height
        )
        layout(width, height) {
            var consumedHeight = 0

            fun Placeable.place() {
                placeRelative(
                    Alignment.CenterHorizontally.align(
                        this.width,
                        width,
                        layoutDirection
                    ),
                    consumedHeight
                )
                consumedHeight += this.height
            }

            header.place()
            emailFormPlaceable.place()
            emailActionsPlaceable.place()
            authProviderPlaceable.place()
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable fun SignInScreen(
    navController: NavHostController,
    user: UserViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    var showProgressIndicator by remember { mutableStateOf(false) }
    if(showProgressIndicator) Popup(alignment = Alignment.Center) {
        CircularProgressIndicator()
    }

    var email    by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var emailError: String?    by rememberSaveable { mutableStateOf(null) }
    var passwordError: String? by rememberSaveable { mutableStateOf(null) }

    val passwordSaver = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { }
    fun emailAction(
        route: Destination,
        action: suspend (email: String, password: String) -> Unit
    ) {
        scope.launch {
            emailError    = null
            passwordError = null
            when {
                email.isEmpty() -> {
                    emailError = "No email provided"
                    return@launch
                }
                password.isEmpty() -> {
                    passwordError = "No password provided"
                    return@launch
                }
            }
            try {
                showProgressIndicator = true
                action(email, password)
                route.navigate(navController) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                }
            } catch (err: FirebaseAuthException) {
                user.handleAuthError(err,
                    onEmailError    = { emailError = it },
                    onPasswordError = { passwordError = it },
                    onAuthError     = { scaffoldState.snackbarHostState.showSnackbar(it) }
                )
            } catch (e: ApiException) {
                // If the saver fails isn't critical
                // May fail if we just used autofill to get details
                Log.e("Sign in/up", "Failed to save the email/password", e)
                route.navigate(navController) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                }
            } finally {
                showProgressIndicator = false
            }
        }
    }

    fun signInAction() = emailAction(Destinations.CalendarView) { email, password ->
        user.authenticate(email, password, GoogleIntentLauncher(passwordSaver))
    }
    fun signUpAction() = emailAction(Destinations.SignUp) { email, password ->
        user.create(email, password, GoogleIntentLauncher(passwordSaver))
    }

    Scaffold(scaffoldState = scaffoldState) {
        SignInScreenLayout(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight()
            ,
            logo = {
                AppLogo(
                    useLarge = true,
                    modifier = Modifier
                        .padding(32.dp)
                )
            },
            emailForm = {
                SignInForm(
                    modifier = Modifier.padding(bottom = 16.dp),
                    email    = email,
                    password = password,
                    onEmailChanged    = { email = it },
                    onPasswordChanged = { password = it },
                    emailError    = emailError,
                    passwordError = passwordError,
                    onFilled      = ::signInAction
                )
            },
            emailActions = {
                EmailActions(
                    modifier = Modifier.padding(8.dp),
                    onSignIn = ::signInAction,
                    onSignUp = ::signUpAction,
                    onForgot = { Destinations.Forgot.navigate(navController) }
                )
            },
            authProvider = {
                var limitOneTap by rememberSaveable { mutableStateOf(3) }
                @Composable fun googleLauncher(route: Destination) = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartIntentSenderForResult()
                ) { scope.launch {
                    try {
                        user.authenticate(GoogleIntent(it.data))
                        route.navigate(navController) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                        }
                    } catch (e: ApiException) {
                        when(e.statusCode) {
                            CommonStatusCodes.CANCELED -> {
                                Log.d("Sign In", "User cancelled sign in w/ One-Tap")
                                limitOneTap--
                            }
                            else -> Log.d("Sign In", "Failed to sign in w/ One-Tap", e)
                        }
                    }
                } }
                val googleSignInLauncher = googleLauncher(Destinations.CalendarView)
                val googleSignUpLauncher = googleLauncher(Destinations.SignUp)

                AuthProviders(
                    modifier = Modifier.padding(8.dp),
                    onGoogle = provideInScope(scope) {
                        try {
                            Log.d("Sign In", "Current limit count: $limitOneTap")
                            if(limitOneTap > 0) user.authenticate(GoogleIntentLauncher(googleSignInLauncher))
                            else scaffoldState.snackbarHostState.showSnackbar("Apologises, usage of Google sign in has been limited")
                        } catch(e: IntentSender.SendIntentException) {
                            Log.e("Sign in", "Failed to start One Tap UI", e)
                        } catch(e: ApiException) {
                            Log.d("Sign in", "No saved credentials found", e)
                            user.create(GoogleIntentLauncher(googleSignUpLauncher))
                        }
                    },
                    googleText = { Text("Continue with Google") }
                )
            }
        )
    }
}
