package uk.co.sksulai.multitasker.ui.component

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * Default padding to be applied to helper text under a text field.
 * It adds 16[dp] to the start of the text and 16[dp] to the top from the baseline
 *
 * @see ErrorText
 * @see HelperText
 */
fun Modifier.textFieldHelperPadding() = this
    .widthIn(max = TextFieldDefaults.MinWidth)
    .paddingFromBaseline(top = 16.dp)
    .padding(horizontal = 16.dp)

/**
 * Used to properly show an error message to the user with the correct formatting
 *
 * @param message   The error message to be show (if empty/null nothing is shown)
 * @param modifier  The modifier to apply [Text]
 * @param textStyle The text style to be applied to the error message
 */
@Composable fun ErrorText(
    message: String?,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current
) {
    if(message?.isNotEmpty() == true) Text(
        text  = message,
        style = textStyle,
        color = MaterialTheme.colors.error,
        modifier = modifier.semantics { error(message) },
    )
}

/**
 * Used to mark labels if the contents of a text field contain an error
 *
 * @param isError Whether the value of the text field contains an error
 * @param label   Text to put in the label
 */
@Composable fun LabelText(
    isError: Boolean,
    label: String,
) = Text(label + if(isError) " *" else "")
/**
 * Used to mark labels if the contents of a text field contain an error
 *
 * @param isError Whether the value of the text field contains an error
 * @param label   Text to put in the label
 */
@Composable fun LabelText(
    isError: Boolean,
    label: @Composable () -> Unit,
) = Row {
    label()
    if(isError) Text("*")
}

/**
 * Helper text of a text field
 *
 * @param text     Helper text to be shown under the text field
 * @param isError  Whether this text is a helper or error message
 * @param modifier Modifier to apply to this component
 */
@Composable fun HelperText(
    text: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) = ProvideTextStyle(MaterialTheme.typography.caption) {
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        if(isError) ErrorText(text, modifier)
        else Text(text, modifier)
    }
}
/**
 * Helper text of a text field
 *
 * @param text     Helper text to be shown under the text field
 * @param error    Error message to be shown in-place of [text] (if not empty)
 * @param modifier Modifier to apply to this component
 */
@Composable fun HelperText(
    text: String,
    error: String?,
    modifier: Modifier
) = HelperText(
    text     = error.takeUnless { it.isNullOrEmpty() } ?: text,
    isError  = !error.isNullOrEmpty(),
    modifier = modifier
)


/**
 * Circular icon which can be used as the leading icon
 * @param colour Colour of the icon
 * @param modifier Modifier to apply to the canvas
 */
@Composable fun ColourIcon(
    colour: Color,
    modifier: Modifier = Modifier
) = Canvas(modifier.defaultMinSize(
    minWidth  = 24.dp,
    minHeight = 24.dp,
)) { drawCircle(colour) }
