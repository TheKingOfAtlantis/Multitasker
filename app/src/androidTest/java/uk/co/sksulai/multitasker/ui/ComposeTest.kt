package uk.co.sksulai.multitasker.ui

import org.junit.Rule
import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

/**
 * Wrapper just so to avoid needing to call composeTestRule everytime
 * But it also ensures that test class is setup for testing composables
 */
abstract class ComposeTest {
    @get:Rule val composeTestRule = createComposeRule()
    /**
     * Calls [ComposeContentTestRule.setContent] on the [block]
     */
    @CallSuper
    open fun setContent(block: @Composable () -> Unit) =
        composeTestRule.setContent(block)
}

/**
 * Extends the functionality of [ComposeTest] to ensure that a navController
 * is provided everytime we set the content
 */
abstract class NavigableComposeTest : ComposeTest() {
    lateinit var navController: NavHostController
    /**
     * Calls [ComposeContentTestRule.setContent] on the [block] and initialises
     * the [navController] each time which can be used inside of [block] to
     * handle navigation
     */
    final override fun setContent(block: @Composable () -> Unit) = super.setContent {
        navController = rememberNavController()
        block()
    }
}
