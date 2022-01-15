package uk.co.sksulai.multitasker.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder


/**
 * Provides a strongly typed representation of a navigation route
 * @param route Route to the destination
 */
sealed class Destination(val route: String) {
    /**
     * Navigates to this destination using a given [NavController]
     * @param navController The controller to use to get to this destination
     * @param builder Optional DSL to specify navigation options
     */
    fun navigate(
        navController: NavController,
        builder: (NavOptionsBuilder.() -> Unit)? = null
    ) = builder?.let { navController.navigate(route, it) } ?: navController.navigate(route)
}
/**
 * Provides a strongly typed representation of a navigation route with additional
 * info including a title and icon
 * @param route Route to the destination
 * @param title String resource containing the name/title for this destination
 * @param icon  Icon to be used to represent this destination
 */
sealed class ExtendedDestination(
    route: String,
    @StringRes val title: Int,
    val icon: ImageVector
) : Destination(route) {
    /**
     * Retrieves the value of the title from the string resource
     */
    @Composable fun getTitleValue() = stringResource(title)
}

object Destinations {
    object SignInFlow : Destination("SignInFlow") // Sign in flow subgraph
    object OnBoarding : Destination("OnBoarding") // Onboarding screen
    object SignIn     : Destination("SignIn")     // Sign in screen
    object SignUp     : Destination("SignUp")     // Sign up screen
    object Forgot     : Destination("Forgot")     // Forgot password screen
}
