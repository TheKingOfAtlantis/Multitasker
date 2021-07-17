package uk.co.sksulai.multitasker.ui.userFlow

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.core.os.bundleOf
import uk.co.sksulai.multitasker.util.getValue

class FieldState(
    textInitial:  String = "",
    errorInitial: String = ""
) {
    var text  by mutableStateOf(textInitial)
    var error by mutableStateOf(errorInitial)
    val valid get() = error.isEmpty()
    var visible by mutableStateOf(false)
}

@Composable fun rememberFieldState(initialText: String = "", initialError: String = "") = rememberSaveable(
    saver = Saver(
        save = { bundleOf(
            "text"       to it.text,
            "error"      to it.error,
            "visibility" to it.visible
        ) },
        restore = {
            val text: String by it
            val error: String by it
            val visibility: Boolean by it

            FieldState(text, error).apply { visible = visibility }
        }
    ),
    init = { FieldState(initialText, initialError) }
)

