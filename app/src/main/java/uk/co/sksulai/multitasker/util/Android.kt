package uk.co.sksulai.multitasker.util

import android.os.Bundle
import kotlin.reflect.KProperty

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> Bundle.getValue(thisObj: Any?, property: KProperty<*>): T =
    @Suppress("UNCHECKED_CAST") get(property.name) as T
