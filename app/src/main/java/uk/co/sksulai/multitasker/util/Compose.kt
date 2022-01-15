package uk.co.sksulai.multitasker.util

import androidx.compose.runtime.*
import androidx.appcompat.app.AppCompatActivity


val LocalActivity = staticCompositionLocalOf<AppCompatActivity> {
    error("CompositionLocal LocalActivity not present")
}

@Composable fun ProvideActivity(
    activity: AppCompatActivity,
    block: @Composable () -> Unit
) = CompositionLocalProvider(
    LocalActivity provides activity,
    content = block
)

/**
 * Simple wrapper to [remember] a [MutableState]
 *
 * @param value Initial value to be passed to [mutableStateOf]
 * @return The remembered mutable state
 *
 * @see remember
 * @see mutableStateOf
 */
@Composable fun <T> rememberMutableState(
    value: T,
    policy: SnapshotMutationPolicy<T> = structuralEqualityPolicy()
) = remember { mutableStateOf(value, policy) }
/**
 * Simple wrapper to [remember] a [MutableState]
 *
 * @param key Given to [remember] to trigger the recalculation of the [MutableState]
 * @param value Initial value to be passed to [mutableStateOf]
 * @return The remembered mutable state
 *
 * @see remember
 * @see mutableStateOf
 */
@Composable fun <T> rememberMutableState(
    vararg key: Any?,
    value: T,
    policy: SnapshotMutationPolicy<T> = structuralEqualityPolicy()
) = remember(key) { mutableStateOf(value, policy) }
