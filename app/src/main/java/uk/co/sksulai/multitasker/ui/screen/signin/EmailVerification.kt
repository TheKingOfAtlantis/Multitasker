package uk.co.sksulai.multitasker.ui.screen.signin

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable fun EmailVerification(
    email: String,
    onSubmitRequest: () -> Unit,
    onCancelRequest: (() -> Unit)? = null,
    preamble: @Composable (() -> Unit)? = null,
) = Column(
    Modifier
        .fillMaxSize()
        .wrapContentSize(),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(
        "Email verification",
        style = MaterialTheme.typography.h6
    )

    preamble?.let {
        Box(Modifier.padding(
            horizontal = 24.dp,
            vertical = 16.dp
        )) {
            preamble()
        }
    }
    Button(onClick = onSubmitRequest) { Text("Send Request") }
    if(onCancelRequest != null)
        TextButton(onClick = onCancelRequest) { Text("Verify later") }
}

