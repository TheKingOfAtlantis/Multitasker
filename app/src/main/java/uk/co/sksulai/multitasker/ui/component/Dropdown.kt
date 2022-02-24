package uk.co.sksulai.multitasker.ui.component

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import uk.co.sksulai.multitasker.util.rememberMutableState

/**
 * Wrapper around ExposedDropdownMenu that automates large potions of creating
 * a dropdown menu. It allows the creation of a dropdown from a list of any type
 * and provides parameters to control how that is visualised to the the user.
 *
 * @param value           Current selected value
 * @param onValueSelected The value selected by the user
 * @param entries         List of values which can be choosen from
 * @param label           Label added to the internal [TextField]/[OutlinedTextField]
 * @param itemText        Used to convert object of type [T] to [String] for the text field
 *                        and dropdown list (if [itemContent] not provided/is null)
 * @param leadingIcon     Icon placed in the leading position of the internal [TextField]/[OutlinedTextField]
 * @param header          Arbitrary composable placed at the top of the dropdown list
 * @param footer          Arbitrary composable placed at the bottom of the dropdown list
 * @param outlined        Whether the text field is [OutlinedTextField] (true) or [TextField] (false)
 * @param itemContent     Contents of each dropdown menu item/entry
 */
@ExperimentalMaterialApi
@Composable fun <T : Any?> Dropdown(
    value: T?,
    onValueSelected: (T) -> Unit,
    entries: List<T>,

    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    itemText: (T?) -> String = { it?.toString() ?: "" },
    leadingIcon: @Composable (() -> Unit)? = null,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    footer: @Composable (ColumnScope.() -> Unit)? = null,
    outlined: Boolean = true,
    itemContent: @Composable (RowScope.(T) -> Unit) = { Text(itemText(it)) },
) {
    var expanded by rememberMutableState(value = false)

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        if(outlined) OutlinedTextField(
            label         = label,
            value         = itemText(value),
            onValueChange = { },
            readOnly      = true,
            leadingIcon   = leadingIcon,
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            colors        = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        ) else TextField(
            label         = label,
            value         = itemText(value),
            onValueChange = { },
            readOnly      = true,
            leadingIcon   = leadingIcon,
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            colors        = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if(header != null) header()

            entries.forEach {
                DropdownMenuItem(onClick = {
                    onValueSelected(it)
                    expanded = true
                }) { itemContent(it) }
            }

            if(footer != null) footer()
        }
    }
}
