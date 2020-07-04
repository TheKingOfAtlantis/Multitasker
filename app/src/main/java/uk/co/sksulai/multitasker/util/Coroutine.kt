package uk.co.sksulai.multitasker.util

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun <T, U> (suspend (U) -> T).asFlow(arg: U): Flow<T> = flow {
    emit(invoke(arg))
}
fun <T, U> ((U) -> T).asFlow(arg: U): Flow<T> = flow {
    emit(invoke(arg))
}
