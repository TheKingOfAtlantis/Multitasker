package uk.co.sksulai.multitasker.util

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

val LocalNavController = staticCompositionLocalOf<NavHostController> { error("CompositionLocal for NavController not present")  }

@Composable fun ProvideNavController(
    content: @Composable () -> Unit
) = CompositionLocalProvider(
    LocalNavController provides rememberNavController(),
    content = content
)
