package uk.co.sksulai.multitasker.ui.screen.signin

import kotlin.random.Random

import org.junit.*
import org.junit.Assert.fail
import com.google.common.truth.Truth.*

import androidx.test.filters.LargeTest
import androidx.compose.ui.test.*

import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.unit.*

import uk.co.sksulai.multitasker.ui.*
import uk.co.sksulai.multitasker.util.*

@LargeTest class SignInScreen : NavigableComposeTest() {

    /**
     * Tests the sign in screen to check for a successful sign in attempt
     * using the sign-in button
     */
    @Test fun SignIn_Success_Button() {
        setContent {

        }
        fail()
    }
    /**
     * Tests the sign in screen to check for a successful sign in attempt
     * using the Ime action to sign in
     */
    @Test fun SignIn_Success_ImeAction() {
        setContent {

        }
        fail()
    }
    }
    /**
     * Tests the sign in screen to check for a successful sign up attempt
     */
    @Test fun SignUp_Success() {
        setContent {

        }
        fail()
    }
    /**
     * Tests the sign in screen to check for a unsuccessful sign in attempt
     * using plausibly valid credentials of a non-existent account
     */
    @Test fun SignIn_NonexistentAccount() {
        setContent {

        }
        fail()
    }
    /**
     * Tests the sign in screen to check for a unsuccessful sign in attempt
     * using an invalid email
     */
    @Test fun SignIn_InvalidEmail() {
        setContent {

        }
        fail()
    }
    /**
     * Tests the sign in screen to check for a unsuccessful sign in attempt
     * using a valid email but a incorrect password
     */
    @Test fun SignIn_InvalidPassword() {
        setContent {

        }
        fail()
    }
    /**
     * Tests the sign in screen to check for a unsuccessful sign in attempt
     * using a valid email but a missing password
     */
    @Test fun SignIn_MissingPassword() {
        setContent {

        }
        fail()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@LargeTest class SignInForm : ComposeTest() {
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

        // FIXME: Requesting focus in LaunchedEffect causes the instrumentation to crash
        //        Feels like a compose problem
        //        Especially since focusing otherwise works in a non-testing environment
        fail("Pre-emptively failing to stop the testing stalling until issue with requesting focus is fixed")

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

        // FIXME: Requesting focus in LaunchedEffect causes the instrumentation to crash
        //        Feels like a compose problem
        //        Especially since focusing otherwise works in a non-testing environment
        fail("Pre-emptively failing to stop the testing stalling until issue with requesting focus is fixed")

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

        // FIXME: Requesting focus in LaunchedEffect causes the instrumentation to crash
        //        Feels like a compose problem
        //        Especially since focusing otherwise works in a non-testing environment
        fail("Pre-emptively failing to stop the testing stalling until issue with requesting focus is fixed")

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
@LargeTest class EmailActions : ComposeTest() {

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

@LargeTest class AuthProvider : ComposeTest() {
    @Test fun googleButton() {
        setContent {

        }
        fail()
    }
}
