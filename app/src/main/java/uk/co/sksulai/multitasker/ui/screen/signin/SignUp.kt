package uk.co.sksulai.multitasker.ui.screen.signin

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.ui.Modifier

import androidx.navigation.*
import androidx.navigation.compose.*
import androidx.hilt.navigation.compose.hiltViewModel
import uk.co.sksulai.multitasker.db.repo.UserRepository

import uk.co.sksulai.multitasker.db.viewmodel.UserViewModel
import uk.co.sksulai.multitasker.util.provideInScope
import uk.co.sksulai.multitasker.util.rememberSaveableMutableState

@Composable fun SignUpScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel()
) {
    val pageController = rememberNavController()

    val user by userViewModel.currentUser.collectAsState(initial = null)

    NavHost(
        navController = pageController,
        startDestination =
            if(userViewModel.emailVerification.verified) UserDetailsRoute
            else EmailVerificationRoute
    ) {
        EmailVerificationPage(
            user?.Email ?: "",
            userViewModel.emailVerification
        ) // Need to verify user email
        UserDetailsPage() // Then add the user's details
    }
}

const val EmailVerificationRoute = "verify"
fun NavGraphBuilder.EmailVerificationPage(
    email: String,
    emailVerification: UserRepository.EmailVerification
) = composable(EmailVerificationRoute) {
    val scope = rememberCoroutineScope()
    var linkSent by rememberSaveableMutableState(false)

    if(!linkSent) EmailVerification(
        email,
        onSubmitRequest = provideInScope(scope) {
            emailVerification.request("signup#details")
            linkSent = true
        },
        preamble = {
            Text(
                "We need to verify you email to prevent spam" +
                    " and to ensure we can reach you if we need to"
            )
        }
    ) else Column(
        Modifier
            .fillMaxSize()
            .wrapContentSize()
    ) {
        Text("Link has been sent")
    }
}

const val UserDetailsRoute = "details"
fun NavGraphBuilder.UserDetailsPage() = composable(UserDetailsRoute) {

}
