package uk.co.sksulai.multitasker.ui

import androidx.compose.*
import androidx.ui.animation.Crossfade
import com.github.zsoltk.compose.router.BackStack
import com.github.zsoltk.compose.router.Router

val MainBackstackAmbient = staticAmbientOf<BackStack<MainState>>()

enum class MainState {
    OnBoarding,
    SignInFlow,
    CalendarView,
    Settings
}

@Composable fun EntryPoint(initialState: MainState?) = Router(initialState ?:
        MainState.CalendarView
) { backstack -> Providers(MainBackstackAmbient provides backstack) {
    Crossfade(current = backstack.last()) {
        when (it) {
            MainState.OnBoarding   -> TODO() // OnBoardingScreen()
            MainState.SignInFlow   -> TODO() // SignInFlowScreen()
            MainState.CalendarView -> TODO() // CalendarViewScreen()
            MainState.Settings     -> TODO() // SettingsScreen()
        }
    }
} }
