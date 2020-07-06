package uk.co.sksulai.multitasker.ui

import androidx.compose.*
import androidx.ui.animation.Crossfade
import com.github.zsoltk.compose.router.BackStack
import com.github.zsoltk.compose.router.Router

val MainBackstackAmbient = staticAmbientOf<BackStack<MainState>>() // Highest backstack
val CurrentBackstackAmbient = ambientOf<BackStack<*>>(
    throw IllegalStateException("backPressHandler is not initialized")
) // Backstack for the current level


enum class MainState {
    OnBoarding,
    SignInFlow,
    CalendarView,
    Settings
}

@Composable fun EntryPoint(initialState: MainState?) = Router(initialState ?:
        MainState.CalendarView
) { backstack -> Providers(
    MainBackstackAmbient provides backstack,
    CurrentBackstackAmbient provides backstack
) {
    Crossfade(current = backstack.last()) {
        when (it) {
            MainState.OnBoarding   -> TODO() // OnBoardingScreen()
            MainState.SignInFlow   -> SignInFlowScreen()
            MainState.CalendarView -> TODO() // CalendarViewScreen()
            MainState.Settings     -> TODO() // SettingsScreen()
        }
    }
} }
