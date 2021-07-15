package uk.co.sksulai.multitasker.util

import android.os.Bundle
import kotlin.reflect.KProperty

@Suppress(
    "NOTHING_TO_INLINE",
    "UNCHECKED_CAST"
) inline operator fun <T> Bundle.getValue(
    thisObj: Any?, property: KProperty<*>
): T = get(property.name) as T
