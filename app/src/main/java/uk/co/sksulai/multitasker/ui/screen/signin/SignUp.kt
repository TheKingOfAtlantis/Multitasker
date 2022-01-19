package uk.co.sksulai.multitasker.ui.screen.signin

import androidx.compose.runtime.Composable

import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel

import uk.co.sksulai.multitasker.db.viewmodel.UserViewModel

@Composable fun SignUpScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel()
) {
    val pageController = rememberNavController()

    NavHost(
        navController = pageController,
        startDestination = TODO()
    ) {
        
    }
}
