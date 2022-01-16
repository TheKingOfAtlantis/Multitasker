package uk.co.sksulai.multitasker.ui.screen.signin

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit

import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.accompanist.pager.*

import uk.co.sksulai.multitasker.ui.Destinations
import uk.co.sksulai.multitasker.ui.component.*
import uk.co.sksulai.multitasker.util.DatastoreLocators.AppState
import uk.co.sksulai.multitasker.util.provideInScope

/**
 * The first page shown in the on-boarding screen welcoming the user to the app
 */
@ExperimentalPagerApi fun PagerBuilder.WelcomePage() = page { Column(
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    AppLogo(
        Modifier.padding(16.dp),
        useLarge = true
    )
    Text("Welcome to Multitasker")
} }

/**
 * The last page show which lets the user move on to signing up/in
 * @param onGetStartedPressed Callback for when the "Get Started" button has been pressed
 */
@ExperimentalPagerApi fun PagerBuilder.StartPage(
    onGetStartedPressed: () -> Unit
) = page { Box(contentAlignment = Alignment.Center) {
    Button(
        onClick = onGetStartedPressed,
        content = { Text("Get Started") } // TODO: Replace with string resource
    )
} }


@OptIn(ExperimentalPagerApi::class)
@Composable fun OnBoardingScreen(
    navController: NavHostController
) = Surface {
    val scope = rememberCoroutineScope()
    val appStateDatastore = AppState.retrieve()
    val pagerState = rememberPagerState()

    HorizontalPager(
        modifier = Modifier.fillMaxSize(),
        state = pagerState
    ) {
        WelcomePage()
        // TODO: Add additional pages showing off the app's "big ideas"
        StartPage(provideInScope(scope) {
            appStateDatastore.edit { it[AppState.OnBoarded] = true }
            Destinations.SignIn.navigate(navController) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
            }
        })
    }
    HorizontalPagerDecoration(pagerState)
}
