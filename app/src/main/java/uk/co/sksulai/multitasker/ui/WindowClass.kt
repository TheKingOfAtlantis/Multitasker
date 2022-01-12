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
@Composable private fun Activity.rememberWindowSize(): Size {
    // WindowMetricsCalculator implicitly depends on the configuration through the activity,
    // so re-calculate it upon changes.
    val windowMetrics = remember(LocalConfiguration.current) {
        WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)
    }
    return windowMetrics.bounds.toComposeRect().size
}

/**
 * Remembers the [WindowSize] class for the window corresponding to the current window metrics.
 */
@Composable fun Activity.rememberWindowSizeClass(): WindowSize {
    val windowSize = rememberWindowSize()                  // Get the size (in pixels) of the window
    return getWindowSizeClass(with(LocalDensity.current) { // Calculate the window size class
        windowSize.toDpSize()                              // Convert the window size to [Dp]
    })
}
