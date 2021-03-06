package uk.co.sksulai.multitasker.ui

import androidx.compose.runtime.*
import androidx.navigation.compose.*
import uk.co.sksulai.multitasker.ui.userFlow.*
import uk.co.sksulai.multitasker.util.*


@Composable fun EntryPoint(initialState: String = "OnBoarding") = ProvideNavController {
    NavHost(LocalNavController.current, initialState) {
        composable("OnBoarding") {  }
        navigation("SignIn", "SignInFlow") {
            composable("SignIn") { }
            composable("SignUp") { }
            composable("SignUp/Done") { }
            composable("Forgot") { }
        }
    }
}
