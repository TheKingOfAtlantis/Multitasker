package uk.co.sksulai.multitasker.ui

import androidx.compose.*
import com.github.zsoltk.compose.router.BackStack
import com.github.zsoltk.compose.router.Router

import android.net.Uri

enum class SignInFlowState {
    SignIn,         // Indicates user on the main sign in page
    SignUp,         // Indicates user on the sign up page
    Forgot,         // Indicates the user has forgot there password
    ForgotDeepLink  // Indicates has accessed the link provided by 'Forgot Password'
}

data class SignInFlowModel(
    var email : String,
    var password: String
)

@Composable fun SignInFlowScreen() {
    var state by state { SignInFlowModel("", "") }

    Router(defaultRouting = SignInFlowState.SignIn) { backstack ->
        Providers(CurrentBackstackAmbient provides backstack) {
            when (backstack.last()) {
                SignInFlowState.SignIn -> SignInScreen(backstack, state)
                SignInFlowState.SignUp -> SignUpScreen(backstack, state)
                SignInFlowState.Forgot -> ForgotPasswordScreen(backstack, state)
                SignInFlowState.ForgotDeepLink -> ForgotPasswordScreen(Uri.EMPTY)
            }
        }
    }
}

@Composable fun SignInScreen(
    backstack: BackStack<SignInFlowState>,
    state: SignInFlowModel
) { }

@Composable fun SignUpScreen(
    backstack: BackStack<SignInFlowState>,
    state: SignInFlowModel
) { }

@Composable fun ForgotPasswordScreen(
    backstack: BackStack<SignInFlowState>,
    state: SignInFlowModel
) { }

@Composable fun ForgotPasswordScreen(deepLink: Uri) {

}
