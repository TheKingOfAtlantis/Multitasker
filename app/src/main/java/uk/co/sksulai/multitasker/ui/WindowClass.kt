package uk.co.sksulai.multitasker.ui

import android.app.Activity
import androidx.window.layout.WindowMetricsCalculator

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import uk.co.sksulai.multitasker.util.LocalActivity

/**
 * Represent different device sizes
 */
enum class WindowSize {
    /** Most phones in portrait mode **/
    Compact,
    /** Most foldables and tablets in portrait mode **/
    Medium,
    /** Most tablets in landscape mode **/
    Expanded
}

/**
 * Partitions a [DpSize] into a enumerated [WindowSize] class.
 * @return The current window size class
 */
private fun getWindowSizeClass(windowDpSize: DpSize): WindowSize = when {
    windowDpSize.width < 0.dp   -> throw IllegalArgumentException("Dp value cannot be negative")
    windowDpSize.width < 600.dp -> WindowSize.Compact
    windowDpSize.width < 840.dp -> WindowSize.Medium
    else                        -> WindowSize.Expanded
}

/**
 * Remembers the [Size] in pixels of the window corresponding to the current window metrics.
 * @return The size of the window
 */
@Composable private fun Activity.rememberWindowSize(): DpSize = with(LocalDensity.current) {
    // WindowMetricsCalculator implicitly depends on the configuration through the activity,
    // so re-calculate it upon changes.
    val windowMetrics = remember(LocalConfiguration.current) {
        WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this@rememberWindowSize)
    }
    windowMetrics.bounds.toComposeRect().size.toDpSize()
}

/**
 * Remembers the [WindowSize] class for the window corresponding to the current window metrics.
 */
@Composable private fun Activity.rememberWindowSizeClass(): WindowSize =
    getWindowSizeClass(rememberWindowSize())

val LocalWindowSize = staticCompositionLocalOf<WindowSize> {
    error("LocalComposition LocalWindowSize was not provided")
}

/**
 * Provides the current [WindowSize] which is used to adapt the UI layout
 * @param activity The activity in which the composition is taking place
 * @param content  The content to be displayed
 */
@Composable fun ProvideWindowSizeClass(
    activity: Activity = LocalActivity.current,
    content: @Composable () -> Unit
) = CompositionLocalProvider(
    LocalWindowSize provides activity.rememberWindowSizeClass(),
    content = content
)
