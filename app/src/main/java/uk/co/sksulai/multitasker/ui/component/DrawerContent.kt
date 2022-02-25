package uk.co.sksulai.multitasker.ui.component

import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role

import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.hilt.navigation.compose.hiltViewModel

import uk.co.sksulai.multitasker.R
import uk.co.sksulai.multitasker.db.viewmodel.UserViewModel
import uk.co.sksulai.multitasker.ui.Destinations

@ExperimentalMaterialApi
@Composable fun ColumnScope.DrawerContent(
    mainNavController: NavHostController,
    calendarNavController: NavHostController,
    user: UserViewModel = hiltViewModel(),
    onCloseDrawer: () -> Unit = {}
) {
    val currentUser by user.currentUser.collectAsState(initial = null)
    currentUser?.let { UserHeader(it) }
    Divider()
    Destinations.calendarDestinations.forEach {
        val currentDestination by calendarNavController.currentBackStackEntryAsState()

        val isCurrent = it.route == currentDestination?.destination?.route
        val modifier = if(isCurrent) Modifier.background(MaterialTheme.colors.primary) else Modifier
        ListItem(
            icon = { Icon(it.icon, null) },
            text = { Text(it.getTitleValue()) },
            modifier = modifier.selectable(
                role = Role.Button,
                selected = isCurrent
            ) {
                onCloseDrawer()
                it.navigate(calendarNavController) {
                    popUpTo(calendarNavController.graph.startDestinationId) {
                        saveState   = true
                    }
                    restoreState    = true
                    launchSingleTop = true
                }

            }
        )
    }
}
