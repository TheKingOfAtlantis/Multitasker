package uk.co.sksulai.multitasker.ui.screen.signin

import kotlin.random.Random

import org.junit.*
import org.junit.runner.RunWith
import androidx.test.filters.LargeTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import dagger.hilt.android.testing.*

import android.app.Application
import javax.inject.Inject

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

import androidx.compose.runtime.*
import androidx.compose.material.Text
import androidx.compose.ui.*
import androidx.compose.ui.test.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*
import androidx.navigation.compose.*

import uk.co.sksulai.multitasker.ui.*
import uk.co.sksulai.multitasker.ui.ComposeTest
import uk.co.sksulai.multitasker.util.*
import uk.co.sksulai.multitasker.db.LocalDB
import uk.co.sksulai.multitasker.db.repo.UserRepository
import uk.co.sksulai.multitasker.db.viewmodel.UserViewModel

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LargeTest class SignInScreenTest : NavigableComposeTest() {
    @get:Rule(order = -2) val hiltTestRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val instantExecutorRule = InstantTaskExecutorRule()

    @Inject lateinit var app: Application
    @Inject lateinit var db: LocalDB
    @Inject lateinit var userRepo: UserRepository
    val viewModel get() = UserViewModel(app, userRepo)

    @Before fun setup() = hiltTestRule.inject()
    @After fun cleanup() = runBlocking {
        FirebaseEmulatorUtil.auth.deleteAccounts()
        FirebaseEmulatorUtil.db.deleteDocuments()
        db.close()
    }

    private val emailField    get() = composeTestRule.onNodeWithTag("EmailField")
    private val passwordField get() = composeTestRule.onNodeWithTag("PasswordField")
    private val emailError    get() = composeTestRule.onNodeWithTag("EmailError")
    private val passwordError get() = composeTestRule.onNodeWithTag("PasswordError")
    private val signInButton  get() = composeTestRule.onNodeWithTag("SignInButton")
    private val signUpButton  get() = composeTestRule.onNodeWithTag("SignUpButton")

    fun setContent() {
        setContent {
            NavHost(navController, "test") {
                composable("test") { SignInScreen(navController, viewModel) }
                composable(Destinations.SignUp.route) { }
                composable(Destinations.CalendarView.route) { }
            }
        }
    }

    /**
     * Ensure that we show a progress indicator to the user when they sign in or up
     */
    @Test fun Both_ProgressIndicator() {
        setContent()
        val credentials = AuthParam.random

        fun assertProgressIndicator() = composeTestRule
            .onNode(isPopup())
            .onChild()
            .assert(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))

        emailField.performTextInput(credentials.email)
        passwordField.performTextInput(credentials.password)

        // Check get progress indicator when signing-in
        signInButton.performClick()
        assertProgressIndicator()
        composeTestRule.mainClock.advanceTimeBy(200)

        // Check get progress indicator when signing-in
        signUpButton.performClick()
        assertProgressIndicator()
    }

    /**
     * Tests the sign in screen to check for a successful sign in attempt
     * using the sign-in button
     */
    @Test fun SignIn_Success_Button() {
        setContent()

        // We'll create a user whom we'll create an account for and then immediately sign out
        val (credentials, user) = runBlocking {
            AuthParam.random
                .let { it to userRepo.create(it.email, it.password) }
                .also { userRepo.signOut() }
        }

        // Fill in the email/password text field and submit via Ime
        emailField.performTextInput(credentials.email)
        passwordField.performTextInput(credentials.password)
        signInButton.performClick()

        // Now check that the user has been updated and that the
        // current destination has changed navController
        runBlocking {
            delay(100)
            assertThat(
                userRepo.currentUser
                    .filterNotNull()
                    .first()
            ).isEqualTo(user)
        }

        composeTestRule.mainClock.advanceTimeBy(100) // Ensures we've actually navigated
        assertThat(navController.currentBackStackEntry?.destination?.route).apply {
            isEqualTo(Destinations.CalendarView.route)
        }
    }
    /**
     * Tests the sign in screen to check for a successful sign in attempt
     * using the Ime action to sign in
     */
    @Test fun SignIn_Success_ImeAction() {
        setContent()

        // We'll create a user whom we'll create an account for and then immediately sign out
        val (credentials, user) = runBlocking {
            AuthParam.random
                .let { it to userRepo.create(it.email, it.password) }
                .also { userRepo.signOut() }
        }

        // Fill in the email/password text field and submit via Ime
        emailField.apply {
            performTextInput(credentials.email)
            performImeAction()
        }
        passwordField.apply {
            performTextInput(credentials.password)
            performImeAction()
        }

        // Now check that the user has been updated and that the
        // current destination has changed navController
        runBlocking {
            assertThat(
                userRepo.currentUser
                    .filterNotNull()
                    .first()
            ).isEqualTo(user)
        }
        composeTestRule.mainClock.advanceTimeBy(100) // Ensures we've actually navigated
        assertThat(navController.currentBackStackEntry?.destination?.route).apply {
            isEqualTo(Destinations.CalendarView.route)
        }
    }
    /**
     * Tests the sign in screen to check for a successful sign up attempt
     */
    @Test fun SignUp_Success() {
        setContent()

        val credentials = AuthParam.random

        // Fill in the email/password text field and submit via Ime
        emailField.performTextInput(credentials.email)
        passwordField.performTextInput(credentials.password)
        signUpButton.performClick()

        // Now check that the user has been updated and that the
        // current destination has changed navController
        runBlocking {
            assertThat(
                userRepo.currentUser
                    .filterNotNull()
                    .first()
            ).isNotNull()
        }

        assertThat(navController.currentBackStackEntry?.destination?.route).apply {
            isEqualTo(Destinations.SignUp.route)
        }
    }
    @Test fun SignUp_WeakPassword() {
        setContent()

        val credentials = AuthParam.random

        // Fill in the email/password text field and submit via Ime
        emailField.performTextInput(credentials.email)
        passwordField.performTextInput(Random.nextString(5))
        signUpButton.performClick()


        passwordError.apply {
            assertExists()
            assertTextContains(UserViewModel.authErrorMessages["ERROR_WEAK_PASSWORD"]!!)
        }
    }
    @Test fun SignUp_InvalidEmail() {
        setContent()

        val credentials = AuthParam.random

        // Fill in the email/password text field and submit via Ime
        emailField.performTextInput(Random.nextString(20))
        passwordField.performTextInput(credentials.password)
        signUpButton.performClick()

        emailError.apply {
            assertExists()
            assertTextContains(UserViewModel.authErrorMessages["ERROR_INVALID_EMAIL"]!!)
        }
    }

    /**
     * Tests the sign in screen to check for a unsuccessful sign in attempt
     * using plausibly valid credentials of a non-existent account
     */
    @Test fun SignIn_NonexistentAccount() {
        setContent()

        // Get credentials for an account which doesn't exist
        val credentials = AuthParam.random

        emailField.performTextInput(credentials.email)
        passwordField.performTextInput(credentials.password)
        signInButton.performClick()

        // Assert that we don't use the email/password fields to show error
        // Instead shown in the snackbar at the bottom
        emailError.assertDoesNotExist()
        passwordError.assertDoesNotExist()
        composeTestRule
            .onNodeWithText(UserViewModel.authErrorMessages["ERROR_USER_NOT_FOUND"]!!)
            .assertExists()
    }
    /**
     * Tests the sign in screen to check for a unsuccessful sign in attempt
     * when no email is provided
     */
    @Test fun SignIn_MissingEmail() {
        setContent()

        // We'll create a user whom we'll create an account for and then immediately sign out
        val credentials = AuthParam.random

        // Don't fill in the email but fill the password field and submit via Ime
        emailField.performImeAction()
        passwordField.apply {
            performTextInput(credentials.password)
            performImeAction()
        }
        emailError.apply {
            assertExists()
            assertTextContains("No email provided")
        }
    }
    /**
     * Tests the sign in screen to check for a unsuccessful sign in attempt
     * using a valid email but a incorrect password
     */
    @Test fun SignIn_InvalidPassword() {
        setContent()

        // We'll create a user whom we'll create an account for and then immediately sign out
        val credentials = AuthParam.random.also {
            runBlocking {
                userRepo.create(it.email, it.password)
                userRepo.signOut()
            }
        }

        emailField.apply { // Pass the email of an actual account
            performTextInput(credentials.email)
            performImeAction()
        }
        passwordField.apply { // Pass a random password
            performTextInput(Random.nextString(12))
            performImeAction()
        }

        passwordError.apply { // Ensure we get a particular error message
            assertExists()
            assertTextContains(UserViewModel.authErrorMessages["ERROR_WRONG_PASSWORD"]!!)
        }
    }
    /**
     * Tests the sign in screen to check for a unsuccessful sign in attempt
     * using a valid email but a missing password
     */
    @Test fun SignIn_MissingPassword() {
        setContent()

        // We'll create a user whom we'll create an account for and then immediately sign out
        val credentials = AuthParam.random.also {
            runBlocking {
                userRepo.create(it.email, it.password)
                userRepo.signOut()
            }
        }

        // Fill in the email/password text field and submit via Ime
        emailField.apply {
            performTextInput(credentials.email)
            performImeAction()
        }
        passwordField.performImeAction()
        passwordError.apply {
            assertExists()
            assertTextContains("No password provided")
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@RunWith(AndroidJUnit4::class)
@LargeTest class SignInFormTest : ComposeTest() {
    private val emailField    get() = composeTestRule.onNodeWithTag("EmailField")
    private val passwordField get() = composeTestRule.onNodeWithTag("PasswordField")
    private val emailError    get() = composeTestRule.onNodeWithTag("EmailError")
    private val passwordError get() = composeTestRule.onNodeWithTag("PasswordError")

    /**
     * Asserts the correspondence between the parameters and the text fields
     */
    @Test fun FieldCorrespondence() {
        val emailState    = mutableStateOf("")
        val passwordState = mutableStateOf("")

        var email    by emailState
        var password by passwordState

        var emailCalls = 0
        var passwordCalls = 0

        setContent {
            EmailCredentialsForm(
                email = email,
                password = password,
                onEmailChanged = {
                    email = it
                    emailCalls++
                },
                onPasswordChanged = {
                    password = it
                    passwordCalls++
                },
                emailError = "",
                passwordError = "",
                onFilled = {},
                passwordVisibility = true
            )
        }
        // Now we check the correspondence

        // First we check that when we modify the email and password values
        email    = Random.nextEmail()
        password = Random.nextString(8..24)

        // Assert the correspondence between the fields and the SignInForm parameters
        // ensuring they contain the correct text value
        emailField.assertTextContains(email)
        passwordField.assertTextContains(password)

        // And that when the user inputs a value that the correct callback is used

        // First lets update the email field
        emailField.performTextClearance()
        assertThat(emailCalls).isEqualTo(1)
        assertThat(passwordCalls).isEqualTo(0)
        assertThat(email).isEmpty()

        val testInput = Random.nextString(20)
        emailField.performTextInput(testInput)
        assertThat(emailCalls).isEqualTo(2)
        assertThat(passwordCalls).isEqualTo(0)
        assertThat(email).isEqualTo(testInput)

        // Then update the password field
        emailCalls = 0

        passwordField.performTextClearance()
        assertThat(passwordCalls).isEqualTo(1)
        assertThat(emailCalls).isEqualTo(0)
        assertThat(password).isEmpty()

        passwordField.performTextInput(testInput)
        assertThat(passwordCalls).isEqualTo(2)
        assertThat(emailCalls).isEqualTo(0)
        assertThat(password).isEqualTo(testInput)
    }

    /**
     * Asserts the behaviour of the each fields Ime action
     */
    @Test fun FieldImeAction() {
        var filledCalls = 0
        val onFilled = { filledCalls += 1 }

        setContent {
            EmailCredentialsForm("", "", {}, {}, "", "", onFilled)
        }

        // Ensure that initially neither field is focused
        emailField.assertIsNotFocused()
        passwordField.assertIsNotFocused()

        // For the email field the ime actions should:
        //  - Request the focus be moved to the password field
        emailField.apply {
            performClick()     // Click the field to make it focused
            performImeAction() // Then perform ime action
            assertThat(filledCalls).isEqualTo(0) // Ensure no calls to onFilled made
        }
        // For the email field the ime actions should:
        //  - Request the focus be removed from the password field
        //  - Submit callback via onFilled
        passwordField.apply {
            assertIsFocused() // We should now be focused on the password field
            performImeAction()

            assertIsNotFocused() // Should not longer be focused
            assertThat(filledCalls).isEqualTo(1) // Ensure that only one call to onFilled has been made
        }

    }


    /**
     * Assert that width of the email and password fields are the same
     */
    private fun assertErrorless_LayoutWidths() {
        val emailBounds    = emailField.getUnclippedBoundsInRoot()
        val passwordBounds = passwordField.getUnclippedBoundsInRoot()
        assertThat(emailBounds.width).isEqualTo(passwordBounds.width)
    }
    /**
     * Asserts that the error messages does not exist
     */
    private fun assertErrorless_NoErrorMessage() {
        emailError.assertDoesNotExist()
        passwordError.assertDoesNotExist()
    }
    /**
     * Ensure that when the error messages are all empty that the layout is as expected:
     *  - Width both both fields are the same
     *  - Error fields are both not in the graph
     */
    fun Errorless(
        email: String,
        password: String
    ) {
        setContent {
            EmailCredentialsForm(email, password, { }, { }, "", "", {})
        }
        assertErrorless_LayoutWidths()
        assertErrorless_NoErrorMessage()
    }

    /**
     * Ensure that when the fields are all empty that the layout is as expected:
     *  - Width both both fields are the same
     *  - Error fields are both not in the graph
     */
    @Test fun EmptyState() = Errorless("", "")
    /**
     * Ensure that when only the email field is filled that the layout is as expected:
     *  - Width both both fields are the same
     *  - Error fields are both not in the graph
     */
    @Test fun ValidEmailFilledOnly() = Errorless(
        email    = Random.nextEmail(),
        password = ""
    )

    /**
     * Ensure that when both fields are all filled that the layout is as expected:
     *  - Width both both fields are the same
     *  - Error fields are both not composed
     */
    @Test fun ValidEmailAndPasswordFilled() = Errorless(
        email    = Random.nextEmail(),
        password = Random.nextString(until = 20)
    )
    /**
     * Ensure that the password visibility toggle works as expected:
     *  - Initially appears in the not visible state
     *  - Pressing the icon makes the password visible
     *  - Pressing it again returns the field to the not visible state
     */
    @Test fun PasswordVisibilityToggle() {
        val password = Random.nextString(until = 20)
        setContent {
            val (visibility, onVisibilityChange) = rememberMutableState(false)
            EmailCredentialsForm(
                email = "",
                password = password,
                onEmailChanged = {},
                onPasswordChanged = {},
                emailError = "",
                passwordError = "",
                onFilled = {},
                passwordVisibility = visibility,
                onVisibilityChange = onVisibilityChange
            )
        }

        composeTestRule.onNodeWithTag("PasswordVisibility").apply {
            assertIsToggleable()

            // Initially should be off/false
            // The text should just be bullet points
            assertIsOff()
            passwordField.assertTextContains('\u2022'.toString().repeat(password.length))

            performClick() // Perform click on the button to toggle

            // Password visibility should be on/true
            // The text should be the actual password
            assertIsOn()
            passwordField.assertTextContains(password)
        }
    }

    /**
     * Ensure that when their is a password error that the following occur:
     *  - Email text field is in the error state
     *  - Email text field is focused
     *  - Email error message width is no bigger than the text fields
     *  - Password error message text is not composed
     */
    @Test fun EmailError_Focused() {
        val emailErrorMessage    = "Email Error"
        val passwordErrorMessage = ""

        setContent {
            EmailCredentialsForm("", "", {}, {}, emailErrorMessage, passwordErrorMessage, {})
        }

        // Ensure that email field is showing error but password field is not
        emailField.apply {
            assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
            assertIsFocused()
        }
        passwordField.apply {
            assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Error))
            assertIsNotFocused()
        }

        // Ensure that the error message is show and that its error message is the one given
        emailError.apply {
            assertExists()
            assertTextContains(emailErrorMessage)
        }
        passwordError.assertDoesNotExist()
    }

    /**
     * Ensure that when their is a password error that the following occur:
     *  - Password text field is in the error state
     *  - Password text field is focused
     *  - Password error message width is no bigger than the text fields
     *  - Email error message text is not composed
     */
    @Test fun PasswordError_Focused()  {
        val emailErrorMessage    = ""
        val passwordErrorMessage = "Password Error"

        setContent {
            EmailCredentialsForm("", "", {}, {}, emailErrorMessage, passwordErrorMessage, {})
        }

        // Ensure that email field is showing error but password field is not
        emailField.apply {
            assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Error))
            assertIsNotFocused()
        }
        passwordField.apply {
            assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
            assertIsFocused()
        }

        // Ensure that the error message is show and that its error message is the one given
        emailError.assertDoesNotExist()
        passwordError.apply {
            assertExists()
            assertTextContains(passwordErrorMessage)
        }
    }

    /**
     * When an error has been detected in both the email and the password
     * we should
     *  - Both text fields are in the error state
     *  - Email text field is focused
     *  - Both error message widths are no bigger than the text fields
     */
    @Test fun BothErrors_Focused()  {
        val emailErrorMessage    = "Email Error"
        val passwordErrorMessage = "Password Error"

        setContent {
            EmailCredentialsForm("", "", {}, {}, emailErrorMessage, passwordErrorMessage, {})
        }

        // Ensure that email field is showing error but password field is not
        emailField.apply {
            assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
            assertIsFocused()
        }
        passwordField.apply {
            assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Error))
            assertIsNotFocused()
        }

        // Ensure that the error message is show and that its error message is the one given
        emailError.apply {
            assertExists()
            assertTextContains(emailErrorMessage)
        }
        passwordError.apply {
            assertExists()
            assertTextContains(passwordErrorMessage)
        }
    }
}

@RunWith(AndroidJUnit4::class)
@LargeTest class EmailActionsTest : ComposeTest() {
    private val signInButton = composeTestRule.onNodeWithTag("SignInButton")
    private val signUpButton = composeTestRule.onNodeWithTag("SignUpButton")
    private val forgotButton = composeTestRule.onNodeWithTag("ForgotButton")

    /**
     * Ensures that when we only have a onSignIn callback that:
     *  - The sign in button is provided filling the full width
     *  - That both the sign up and forgot password button are not composed
     */
    @Test fun SignInButtonOnly() {
        var layoutSize: DpSize? = null
        setContent {
            EmailActions(
                onSignIn = { },
                signInLabel = "Sign-in",
                modifier = Modifier.onGloballyPositioned {
                    with(composeTestRule.density) {
                        layoutSize = it.size.toSize().toDpSize()
                    }
                }
            )
        }
        signInButton.assertWidthIsEqualTo(layoutSize!!.width)
        signUpButton.assertDoesNotExist()
        forgotButton.assertDoesNotExist()
    }
    /**
     * Ensures that when we have both a onSignIn and onSignUp callback that:
     *  - The sign in button is provided filling half the width
     *  - The sign up button is provided filling the other half (both are the same width)
     *  - That the forgot password button is not composed
     */
    @Test fun SignInAndUpButtons() {
        var layoutSize: DpSize? = null
        setContent {
            EmailActions(
                onSignIn = { },
                onSignUp = { },
                signInLabel = "Sign-in",
                signUpLabel = "Sign-up",
                modifier = Modifier.onGloballyPositioned {
                    with(composeTestRule.density) {
                        layoutSize = it.size.toSize().toDpSize()
                    }
                }
            )
        }
        val width = (layoutSize!!.width - 8.dp)/2
        signInButton.assertWidthIsEqualTo(width)
        signUpButton.assertWidthIsEqualTo(width)
        forgotButton.assertDoesNotExist()

        // Assert positioning
        val signInBounds = signInButton.getUnclippedBoundsInRoot()
        val signUpBounds = signUpButton.getUnclippedBoundsInRoot()

        assertThat(signInBounds.top).isEqualTo(signUpBounds.top) // Both should be at the same level
        assertThat(signInBounds.right).isLessThan(signUpBounds.left) // Sign in should be to the left/start of sign up
    }
    /**
     * Ensures that when we have both a onSignIn and onSignUp callback that:
     *  - The sign in button is provided filling half the width
     *  - The sign up button is provided filling the other half (both are the same width)
     *  - The forgot password button is provided at fill width below the sign in/up buttons
     *  - Assert parameter correspondence to button properties:
     *      - onSignIn should be triggered by clicking the sign in button
     *      - onSignUp should be triggered by clicking the sign up button
     *      - onForgot should be triggered by clicking the forgot password button
     *      - The sign in button label parameter should adjusts the label of the sign in button
     *      - The sign up button label parameter should adjusts the label of the sign up button
     *      - The forgot password button label parameter should adjusts the label of the forgot password button
     */
    @Test fun AllButtons() {
        var layoutSize: DpSize? = null

        var onSignInCalls = 0
        var onSignUpCalls = 0
        var onForgotCalls = 0
        
        fun resetCallCount() {
            onSignInCalls = 0
            onSignUpCalls = 0
            onForgotCalls = 0
        }

        setContent {
            EmailActions(
                onSignIn = { onSignInCalls++ },
                onSignUp = { onSignUpCalls++ },
                onForgot = { onForgotCalls++ },
                signInLabel = "Sign-in",
                signUpLabel = "Sign-up",
                forgotPasswordLabel = "Forgot Password",
                modifier = Modifier.onGloballyPositioned {
                    with(composeTestRule.density) {
                        layoutSize = it.size.toSize().toDpSize()
                    }
                }
            )
        }

        val width = (layoutSize!!.width - 8.dp)/2
        signInButton.assertWidthIsEqualTo(width)
        signUpButton.assertWidthIsEqualTo(width)
        forgotButton.assertWidthIsEqualTo(layoutSize?.width!!)

        // Assert positioning
        val signInBounds = signInButton.getUnclippedBoundsInRoot()
        val signUpBounds = signUpButton.getUnclippedBoundsInRoot()
        val forgotBounds = forgotButton.getUnclippedBoundsInRoot()

        assertThat(signInBounds.top).isEqualTo(signUpBounds.top)        // Both should be at the same level
        assertThat(signInBounds.right).isLessThan(signUpBounds.left)    // Sign in should be to the left/start of sign up
        assertThat(forgotBounds.top).isGreaterThan(signInBounds.bottom) // Forgot should be below sign in button
        assertThat(forgotBounds.left).isEqualTo(signInBounds.left)      // Forgot should start for the start of the sign in button
        assertThat(forgotBounds.right).isEqualTo(signUpBounds.right)    // and start at the end of the sign up button

        //// Assert field correspondence
        // Assert labels assigned to correct button
        signInButton.assertTextContains("Sign-in")
        signUpButton.assertTextContains("Sign-up")
        forgotButton.assertTextContains("Forgot Password")
        // Assert onClick callback assigned to correct button
        signInButton.apply {
            assertHasClickAction()
            performClick()
            assertThat(onSignInCalls).isEqualTo(1)
            assertThat(onSignUpCalls).isEqualTo(0)
            assertThat(onForgotCalls).isEqualTo(0)
            resetCallCount()
        }
        signUpButton.apply {
            assertHasClickAction()
            performClick()
            assertThat(onSignInCalls).isEqualTo(0)
            assertThat(onSignUpCalls).isEqualTo(1)
            assertThat(onForgotCalls).isEqualTo(0)
            resetCallCount()
        }
        forgotButton.apply {
            assertHasClickAction()
            performClick()
            assertThat(onSignInCalls).isEqualTo(0)
            assertThat(onSignUpCalls).isEqualTo(0)
            assertThat(onForgotCalls).isEqualTo(1)
            resetCallCount()
        }
    }
}

typealias AuthProviderEnum = UserRepository.AuthProvider

@RunWith(AndroidJUnit4::class)
@LargeTest class AuthProviderTest : ComposeTest() {
    /**
     * List of providers that should be available in the AuthProvider
     */
    private val providers = listOf(
        AuthProviderEnum.Google
    )

    /**
     * Used to retrieve the column containing the preamble and button
     *
     * @param tag   The tag associated with the specific auth provider
     * @param block Callback which performs the testing on the provider
     */
    private fun onTestProvider(
        tag: String,
        block: (
            /** The root single provider layout node */
            root: SemanticsNodeInteraction,
            /** The preamble in the provider layout **/
            preamble: SemanticsNodeInteraction,
            /** The button in the provider layout **/
            button: SemanticsNodeInteraction,
        ) -> Unit
    ) {
        // Ensure only one provider with the tag
        composeTestRule
            .onAllNodesWithTag(tag)
            .assertCountEquals(1)

        // Get all the components and pass it to [block]
        composeTestRule.onNodeWithTag(tag).let {
            it.onChildren().apply {
                val preamble = filterToOne(hasTestTag("preamble"))
                val button   = filterToOne(hasTestTag("button"))
                block(it, preamble, button)
            }
        }
    }
    /**
     * Asserts the behaviour of AuthProvider without preamblesApplies to all auth
     * providers including those yet to be added
     */
    @Test fun withoutPreamble() {
        val buttonText   = providers.associateWith { "${it.name} button text" }
        val onCalls      = providers.associateWith { 0 }.toMutableMap()

        setContent {
            AuthProviders(
                onGoogle = { onCalls[AuthProviderEnum.Google] = onCalls[AuthProviderEnum.Google]!! + 1 },
                googleText = { Text(buttonText[AuthProviderEnum.Google]!!) },
                googlePreamble = null
            )
        }

        providers.forEach {
            onTestProvider(it.name) { _, preamble, button ->
                preamble.assertDoesNotExist()
                button.apply {
                    assertTextContains(buttonText[it]!!)
                    assertHasClickAction()
                    performClick()
                    assertThat(onCalls[it]!!).isEqualTo(1)
                }
            }
        }
    }
    /**
     * Asserts the behaviour of AuthProvider with preamble. Applies to all auth
     * providers including those yet to be added
     */
    @Test fun withPreamble() {
        val buttonText   = providers.associateWith { "${it.name} button text" }
        val preambleText = providers.associateWith { "${it.name} preamble text" }
        val onCalls      = providers.associateWith { 0 }.toMutableMap()

        setContent {
            AuthProviders(
                onGoogle = { onCalls[AuthProviderEnum.Google] = onCalls[AuthProviderEnum.Google]!! + 1 },
                googleText = { Text(buttonText[AuthProviderEnum.Google]!!) },
                googlePreamble = { Text(preambleText[AuthProviderEnum.Google]!!) }
            )
        }

        providers.forEach {
            onTestProvider(it.name) { _, preamble, button ->
                // Assert association between parameters and auth provider preamble + button
                preamble.assertTextContains(preambleText[it]!!)
                button.apply {
                    assertTextContains(buttonText[it]!!)
                    assertHasClickAction()
                    performClick()
                    assertThat(onCalls[it]!!).isEqualTo(1)
                }
                // Assert relative positioning of preamble and button
                // Ensuring preamble is place above the button
                val preambleBounds = preamble.getUnclippedBoundsInRoot()
                val buttonBounds = button.getUnclippedBoundsInRoot()

                assertThat(preambleBounds.bottom).isLessThan(buttonBounds.top)
            }
        }
    }
}
