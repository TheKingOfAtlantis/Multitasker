package uk.co.sksulai.multitasker.ui

import androidx.compose.runtime.*
import androidx.navigation.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import uk.co.sksulai.multitasker.db.viewmodel.UserViewModel

import uk.co.sksulai.multitasker.ui.userFlow.*
import uk.co.sksulai.multitasker.util.*

@Composable fun EntryPoint() = ProvideNavController {
    val userViewModel = viewModel<UserViewModel>()
    val statePref     = sharedPreferences("state")
    val initialState  = remember {
        // Check if this is the first launch
        if(!statePref.getBoolean("been_onboarded", false)) "OnBoarding"
        // Check if a user is currently signed in, if not direct to sign in/sign up
        else if(userViewModel.currentUser.value == null) "SignInFlow"
        // Otherwise direct to calendar view TODO: direct preferred "Home"
        else "CalendarView"
    }

    NavHost(LocalNavController.current, initialState) {
        composable("OnBoarding") { OnBoardingScreen() }
        navigation("SignIn", "SignInFlow") {
            composable("SignIn") { SignInScreen() }
            composable(
                "SignUp?email={email}&password={password}",
                arguments = listOf(
                    navArgument("email") { },
                    navArgument("password") { },
                )
            ) {
                it.arguments?.let { bundle ->
                    val email    : String by bundle
                    val password : String by bundle
                    SignUpScreen(email, password)
                }
            }
            composable("SignUp/Done") { }
            composable("Forgot") { }
        }
    }
}
