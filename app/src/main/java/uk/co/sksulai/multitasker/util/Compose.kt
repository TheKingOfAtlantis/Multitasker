package uk.co.sksulai.multitasker.util

import kotlinx.coroutines.*
import androidx.compose.runtime.*
import androidx.appcompat.app.AppCompatActivity

/**
 * Provides the [AppCompatActivity] in which this composition is taking place in
 * @see ProvideActivity
 */
val LocalActivity = staticCompositionLocalOf<AppCompatActivity> {
    error("CompositionLocal LocalActivity not present")
}

/**
 * Used to provide the current activity to make it accessible during composition
 *
 * @param activity The activity to be provided to the composition
 * @param content  The contents of the composition which will be able to access the value of [activity]
 */
@Composable fun ProvideActivity(
    activity: AppCompatActivity,
    content: @Composable () -> Unit
) = CompositionLocalProvider(
    LocalActivity provides activity,
    content = content,
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

/**
 * Binds another lambda which can be launched in the given [CoroutineScope]
 *
 * @param scope Coroutine scope to be used to run [invocable]
 * @param invocable Function/lambda to run in the coroutine
 *
 * @return Lambda which calls [launch] with will run the given [invocable]
 *         using the given [scope]
 */
inline fun provideInScope(
    scope: CoroutineScope,
    crossinline invocable: suspend () -> Unit
): () -> Unit = { scope.launch { invocable() } }
