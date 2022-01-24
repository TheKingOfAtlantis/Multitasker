package uk.co.sksulai.multitasker.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Creates a scrim
 *
 * @param visible  Whether or not to show a scrim
 * @param modifier Modifier to be applied to the scrim
 * @param alpha    Alpha to apply to the scrim (default: 33%)
 * @param colour   Colour of the scrim (default: [Color.Black])
 * @param onDismissRequest Used to handle the request to hide the scrim
 */
@ExperimentalMaterialApi
@Composable fun Scrim(
    visible: Boolean,
    modifier: Modifier = Modifier,
    alpha: Float = .33f,
    colour: Color = Color.Black,
    onDismissRequest: () -> Unit
) {
    if (visible) Surface(
        modifier   = modifier.fillMaxSize(),
        color      = colour.copy(alpha),
        onClick    = onDismissRequest,
        indication = null,
        content    = { }
    )
}
