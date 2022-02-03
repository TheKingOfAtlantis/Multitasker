package uk.co.sksulai.multitasker.ui.component

import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.foundation.text.*
import androidx.compose.foundation.interaction.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.*

import uk.co.sksulai.multitasker.util.rememberSaveableMutableState

/**
 * An outlined text field which allows the user to enter a numerical value
 *
 * @param value         The current value
 * @param onValueChange Callback which is triggered when a new value is provided by the user
 * @param format        This string is used to define the integer format
 * @param onFormatError Callback which is triggered when the parsing the input. The provided string
 *                      and exception thrown are provided to resolve or report back to the user
 * @param enabled       Controls the enabled state of the [OutlinedTextField]
 * @param textStyle     Style to be applied to the input text
 * @param label         Optional label to show in the [OutlinedTextField]
 * @param placeholder   Text to display when text field is empty
 * @param leadingIcon   Optional icon to display at the beginning of the text field
 * @param trailingIcon  Optional icon to display at the end of the text field
 * @param isError       Indicates if the current value is an error
 * @param imeAction     The IME action
 * @param keyboardActions when the input service emits an IME action, the corresponding callback
 *                      is called. Note that this IME action may be different from what you specified in [imeAction]
 * @param interactionSource Representing the stream of [Interaction]s for this OutlinedTextField.
 *                      You can create and pass in your own remembered [MutableInteractionSource]
 *                      if you want to observe [Interaction]s and customize the appearance/behavior
 *                      of this OutlinedTextField in different [Interaction]s.
 * @param shape         The shape of the text field's border
 * @param colors        [TextFieldColors] that will be used to resolve color of the text and content
 *                      for this text field in different states. See [TextFieldDefaults.outlinedTextFieldColors]
 */
@Composable fun NumberField(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    format: String = "%d",
    onFormatError: (String, Throwable) -> Unit,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.small,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors()
) {
    var text by rememberSaveableMutableState(format.format(value), value)
    OutlinedTextField(
        value = text,
        onValueChange = {
            try {
                text = it
                onValueChange(it.toInt())
            } catch(e: Throwable) {
                onFormatError(it, e)
            }
        },
        modifier = modifier,
        enabled  = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        singleLine = true,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
    )
}
