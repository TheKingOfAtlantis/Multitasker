package uk.co.sksulai.multitasker.ui.component.graphics

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import uk.co.sksulai.multitasker.R
import uk.co.sksulai.multitasker.ui.component.ColourIcon
import uk.co.sksulai.multitasker.ui.component.Dropdown
import uk.co.sksulai.multitasker.util.rememberMutableState

@ExperimentalMaterialApi
@Composable fun ColourDropdown(
    modifier: Modifier = Modifier,
    value: NamedColour,
    onValueChange: (NamedColour) -> Unit,
    label: @Composable () -> Unit = { Text(stringResource(R.string.colour)) },
    header: @Composable (ColumnScope.() -> Unit)? = null,
    colours: List<NamedColour> = DefaultColours,
) {
    var showColourPicker by rememberMutableState(false)

    Dropdown(
        value = value,
        onValueSelected = onValueChange,
        entries = colours,
        label    = label,
        modifier = modifier,
        itemText = { it?.name ?: "" },
        leadingIcon = { ColourIcon(value.colour, Modifier.size(24.dp)) },
        header = header,
        footer = {
            Divider()
            DropdownMenuItem(onClick = { showColourPicker = true }) {
                ListItem(
                    icon = { Icon(Icons.Default.Edit, null) },
                    text = { Text(stringResource(R.string.colour_custom)) },
                )
            }
        },
        itemContent = {
            ListItem(
                icon = { ColourIcon(it.colour, Modifier.size(24.dp)) },
                text = { Text(it.name) },
            )
        }
    )

    // TODO: Create Colour Picker

//    if(showColourPicker) ColourPickerDialog(
//        value = value,
//        onValueChange = {
//            onValueChange(NamedColour(R.string.colour_custom, it))
//            showColourPicker = false
//        },
//        onDismissDialog = { showColourPicker = false },
//        title = { Text("Custom Calendar Colour") }
//    )
}
