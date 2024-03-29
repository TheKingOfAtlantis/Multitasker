package uk.co.sksulai.multitasker.util

import kotlinx.coroutines.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.activity.ComponentActivity

/**
 * Provides the [AppCompatActivity] in which this composition is taking place in
 * @see ProvideActivity
 */
val LocalActivity = staticCompositionLocalOf<ComponentActivity> {
    error("CompositionLocal LocalActivity not present")
}

/**
 * Used to provide the current activity to make it accessible during composition
 *
 * @param activity The activity to be provided to the composition
 * @param content  The contents of the composition which will be able to access the value of [activity]
 */
@Composable fun ProvideActivity(
    activity: ComponentActivity,
    content: @Composable () -> Unit
) = CompositionLocalProvider(
    LocalActivity provides activity,
    content = content,
)

@Composable fun <T> rememberSaveableMutableState(
    value: T,
    stateSaver: Saver<T, out Any> = autoSaver(),
    policy: SnapshotMutationPolicy<T> = structuralEqualityPolicy()
) = rememberSaveable(stateSaver = stateSaver) { mutableStateOf(value, policy) }

@Composable fun <T> rememberSaveableMutableState(
    value: T,
    vararg inputs: Any?,
    policy: SnapshotMutationPolicy<T> = structuralEqualityPolicy(),
    stateSaver: Saver<T, out Any> = autoSaver(),
    key: String? = null,
) = rememberSaveable(
    inputs = inputs,
    stateSaver = stateSaver,
    key = key
) { mutableStateOf(value, policy) }

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

/**
 * Binds another lambda which takes a parameter which is launched
 * in the given [CoroutineScope]
 *
 * @param scope Coroutine scope to be used to run [invocable]
 * @param invocable Function/lambda to run in the coroutine
 *
 * @return Lambda which calls [launch] with will run the given [invocable]
 *         using the given [scope]
 */
inline fun <T> provideInScopeWithParam(
    scope: CoroutineScope,
    crossinline invocable: suspend (T) -> Unit
): (T) -> Unit = { scope.launch { invocable(it) } }
