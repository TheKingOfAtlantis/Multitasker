package uk.co.sksulai.multitasker.util

import androidx.compose.runtime.*

import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.platform.LocalContext

val LocalNavController = staticCompositionLocalOf<NavHostController> { error("CompositionLocal for NavController not present")  }

@Composable fun ProvideNavController(
    content: @Composable () -> Unit
) = CompositionLocalProvider(
    LocalNavController provides rememberNavController(),
    content = content
)

@Composable fun sharedPreferences(name: String, mode: Int = Context.MODE_PRIVATE): SharedPreferences =
    LocalContext.current.getSharedPreferences(name, mode)
