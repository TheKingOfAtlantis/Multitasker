package uk.co.sksulai.multitasker.ui.screen.calendar

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.*
import uk.co.sksulai.multitasker.db.viewmodel.CalendarViewModel
import uk.co.sksulai.multitasker.ui.Destinations



@OptIn(
    ExperimentalMaterialNavigationApi::class,
) @Composable fun CalendarScreen(
    navController: NavHostController,
    calendarViewModel: CalendarViewModel = hiltViewModel()
) {
    val bottomSheetNavigator  = rememberBottomSheetNavigator()
    val calendarNavController = rememberNavController(bottomSheetNavigator)


    @Composable fun fab() = FloatingActionButton(
        onClick = { Destinations.CalendarCreation.navigate(calendarNavController) },
        content = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
        }
    )
    @Composable fun bottomAppBar() = BottomAppBar {
    }

    ModalBottomSheetLayout(
        bottomSheetNavigator
    ) {
        Scaffold(
            bottomBar = { bottomAppBar() },
            floatingActionButton = { fab() },
            isFloatingActionButtonDocked = true,
            floatingActionButtonPosition = FabPosition.Center
        ) {
            NavHost(calendarNavController, Destinations.Dashboard.route) {
                composable(Destinations.Dashboard.route) { Text("Dashboard") }
                composable(Destinations.Agenda.route) { Text("Agenda") }
                composable(Destinations.Day.route) { Text("Day") }
                composable(Destinations.Week.route) { Text("Week") }
                composable(Destinations.Month.route) { Text("Month") }

                bottomSheet(Destinations.CalendarCreation.route) { /*CalendarCreation(calendarNavController, calendarViewModel)*/ }
                bottomSheet(Destinations.EventCreation.route) { /*EventCreation(calendarNavController, calendarViewModel)*/ }
            }
        }
    }
}


