package uk.co.sksulai.multitasker.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import com.google.accompanist.placeholder.material.placeholder

import uk.co.sksulai.multitasker.db.model.UserModel
import uk.co.sksulai.multitasker.db.model.avatarPainter

/**
 * Header component which is used to show a summary of the current
 * signed in account
 *
 * @param user The user to show in the header
 */
@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalCoilApi::class
) @Composable fun UserHeader(user: UserModel) = ListItem(
    icon = {
        val painter = user.avatarPainter
        Image(
            modifier = Modifier
                .size(46.dp)
                .placeholder(painter.state is ImagePainter.State.Loading),
            painter = painter,
            contentDescription = null
        )
    },
    text = { Text(user.displayName) },
    secondaryText = { Text(user.email) },
    singleLineSecondaryText = true
)
