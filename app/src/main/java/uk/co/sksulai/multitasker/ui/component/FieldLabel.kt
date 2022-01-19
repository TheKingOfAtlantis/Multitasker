package uk.co.sksulai.multitasker.ui.component

import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle

/**
 * Used to properly show an error message to the user with the correct formatting
 *
 * @param message  The error message to be show (If empty/null nothing is shown)
 * @param modifier The modifier to apply [Text]
 * @param textStyle The text style to be applied to the error message
 */
@Composable fun ErrorText(
    message: String?,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current
){
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
 * @param label Text to put in the label
 * @param isError Whether the value of the text field contains an error
 */
@Composable fun LabelText(
    label: String,
    isError: Boolean
) = Text(label + if(isError) " *" else "")
