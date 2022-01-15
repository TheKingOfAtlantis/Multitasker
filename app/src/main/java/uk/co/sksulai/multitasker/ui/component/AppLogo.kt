package uk.co.sksulai.multitasker.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview

import uk.co.sksulai.multitasker.R

@Preview @Composable fun AppLogoPreview() = Surface {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AppLogo()
        AppLogo(useLarge = true)
    }
}

/**
 * Used to add the App's logo
 *
 * @param modifier Modifier to be applied to the [Image]
 * @param useLarge Indicates that the large variant should be drawn
 * @param color    Colour to tint the logo
 */
@Composable fun AppLogo(
    modifier: Modifier = Modifier,
    useLarge: Boolean = false,
    color: Color = LocalContentColor.current
) = Image(
    modifier = modifier,
    painter = painterResource(
        if(useLarge) R.drawable.ic_logo
        else R.drawable.ic_logo_small
    ),
    contentDescription = stringResource(R.string.app_logo),
    colorFilter = ColorFilter.tint(color)
)

