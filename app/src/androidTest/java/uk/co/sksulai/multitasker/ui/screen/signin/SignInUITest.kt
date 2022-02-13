package uk.co.sksulai.multitasker.ui.screen.signin

import javax.inject.Inject

import org.junit.*
import org.junit.Assert.fail
import com.google.common.truth.Truth.*

import androidx.test.filters.LargeTest
import androidx.compose.ui.test.*
import kotlinx.coroutines.runBlocking

import uk.co.sksulai.multitasker.ui.*
import uk.co.sksulai.multitasker.util.*
import uk.co.sksulai.multitasker.db.LocalDB
import uk.co.sksulai.multitasker.db.repo.UserRepository

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
@LargeTest class SignInForm : ComposeTest() {
    /**
     * Ensure that when the fields are all empty that the layout is as expected:
     *  - Width both both fields are the same
     *  - Error fields are both not in the graph
     */
    @Test fun EmptyState() {
        setContent {

        }
        fail()
    }
    /**
     * Ensure that when the fields are all empty that the layout is as expected:
     *  - Width both both fields are the same
     *  - Error fields are both not in the graph
     */
    @Test fun ValidEmailFilledOnly() {
        setContent {

        }
        fail()
    }
    /**
     * Ensure that when both fields are all filled that the layout is as expected:
     *  - Width both both fields are the same
     *  - Error fields are both not composed
     */
    @Test fun ValidEmailAndPasswordFilled() {
        setContent {

        }
        fail()
    }

    /**
     * Ensure that the password visibility toggle works as expected:
     *  - Initially appears in the not visible state
     *  - Pressing the icon makes the password visible
     *  - Pressing it again returns the field to the not visible state
     */
    @Test fun PasswordVisibilityToggle() {
        setContent {

        }
        fail()
    }

    /**
     * Ensure that when their is a password error that the following occur:
     *  - Email text field is in the error state
     *  - Email text field is focused
     *  - Email error message width is no bigger than the text fields
     *  - Password error message text is not composed
     */
    @Test fun EmailError_Focused() {
        setContent {

        }
        fail()
    }

    /**
     * Ensure that when their is a password error that the following occur:
     *  - Password text field is in the error state
     *  - Password text field is focused
     *  - Password error message width is no bigger than the text fields
     *  - Email error message text is not composed
     */
    @Test fun PasswordError_Focused() {
        setContent {

        }
        fail()
    }

    /**
     * When an error has been detected in both the email and the password
     * we should
     *  - Both text fields are in the error state
     *  - Email text field is focused
     *  - Both error message widths are no bigger than the text fields
     */
    @Test fun BothErrors_Focused() {
        setContent {

        }
        fail()
    }
}
@LargeTest class EmailActions : ComposeTest() {
    /**
     * Ensures that when we only have a onSignIn callback that:
     *  - The sign in button is provided filling the full width
     *  - That both the sign up and forgot password button are not composed
     *  - Assert that the sign in button label parameter adjusts the label of the sign in button
     */
    @Test fun SignInButtonOnly() {
        setContent {

        }
        fail()
    }
    /**
     * Ensures that when we have both a onSignIn and onSignUp callback that:
     *  - The sign in button is provided filling half the width
     *  - The sign up button is provided filling the other half (both are the same width)
     *  - That the forgot password button is not composed
     */
    @Test fun SignInAndUpButtons() {
        setContent {

        }
        fail()
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
        setContent {

        }
        fail()
    }
}

@LargeTest class AuthProvider : ComposeTest() {
    @Test fun googleButton() {
        setContent {

        }
        fail()
    }
}
