package uk.co.sksulai.multitasker.util

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import kotlin.reflect.KProperty

@Suppress(
    "NOTHING_TO_INLINE",
    "UNCHECKED_CAST"
) inline operator fun <T> Bundle.getValue(
    thisObj: Any?, property: KProperty<*>
): T = get(property.name) as T

val AndroidViewModel.applicationContext get() = getApplication<Application>().applicationContext
