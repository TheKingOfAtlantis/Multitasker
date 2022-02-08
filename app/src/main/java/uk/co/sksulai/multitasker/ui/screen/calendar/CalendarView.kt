package uk.co.sksulai.multitasker.ui.screen.calendar

import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp

import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel

import com.google.accompanist.navigation.material.*

import uk.co.sksulai.multitasker.db.viewmodel.CalendarViewModel
import uk.co.sksulai.multitasker.ui.Destinations
import uk.co.sksulai.multitasker.ui.component.Scrim
import uk.co.sksulai.multitasker.util.rememberMutableState


@ExperimentalMaterialApi
@ExperimentalMaterialNavigationApi
@Composable fun rememberBottomSheetShape(
    state: BottomSheetNavigatorSheetState
): State<RoundedCornerShape> {
    val animationFraction by run {
        rememberUpdatedState(when(state.progress.to) {
            ModalBottomSheetValue.Expanded     -> 1 - state.progress.fraction
            ModalBottomSheetValue.HalfExpanded -> state.progress.fraction
            ModalBottomSheetValue.Hidden       ->
                if(state.progress.from == ModalBottomSheetValue.Expanded)
                    state.progress.fraction
                else 1f
        })
    }
    return rememberUpdatedState(RoundedCornerShape(
        topStart = lerp(0.dp, 24.dp, animationFraction),
        topEnd   = lerp(0.dp, 24.dp, animationFraction)
    ))
}

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalMaterialNavigationApi::class,
) @Composable fun CalendarScreen(
    navController: NavHostController,
    calendarViewModel: CalendarViewModel = hiltViewModel()
) {
    val bottomSheetNavigator  = rememberBottomSheetNavigator()
    val calendarNavController = rememberNavController(bottomSheetNavigator)


    var expandFabMenu by rememberMutableState(false)
    val fabAnim by animateFloatAsState(targetValue = if(expandFabMenu) 1f else 0f)
    @Composable fun fab() = FloatingActionButton(
        onClick = { expandFabMenu = !expandFabMenu },
        content = {
            Icon(
                modifier = Modifier.rotate(45f * fabAnim),
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
        }
    )
    @Composable fun fabMenu(modifier: Modifier = Modifier) = Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        fun enter(offset: Int) = fadeIn() + slideInVertically { offset*it/2 }
        fun exit(offset: Int)  = slideOutVertically { offset*it/2 } + fadeOut()

        AnimatedVisibility(
            visible  = expandFabMenu,
            enter    = enter(5),
            exit     = exit(5)
        ) {
            ExtendedFloatingActionButton(
                text = { Text("Create Calendar") },
                onClick = {
                    Destinations.CalendarCreation.navigate(calendarNavController)
                    expandFabMenu = false
                },
            )
        }
        AnimatedVisibility(
            visible  = expandFabMenu,
            enter    = enter(3),
            exit     = exit(3)
        ) {
            ExtendedFloatingActionButton(
                modifier = Modifier.padding(top = 8.dp),
                text = { Text("Create Event") },
                onClick = {
                    Destinations.EventCreation.navigate(calendarNavController)
                    expandFabMenu = false
                },
            )
        }
    }

    @Composable fun bottomAppBar() = BottomAppBar {
    }
    
    @Composable fun mainContent() = NavHost(calendarNavController, Destinations.Dashboard.route) {
        composable(Destinations.Dashboard.route) { /*DashboardView()*/ }
        composable(Destinations.Agenda.route) { /*AgendaView(calendarViewModel, currentDate, { currentDate = it })*/ }
        composable(Destinations.Day.route) { /*DayView(currentDate)*/ }
        composable(Destinations.Week.route) { /*WeekView(currentDate)*/ }
        composable(Destinations.Month.route) { /*MonthView(currentDate)*/ }

        bottomSheet(Destinations.CalendarCreation.route) { CalendarCreation({ calendarNavController.navigateUp() }, calendarViewModel) }
        bottomSheet(Destinations.EventCreation.route) { /*EventCreation(calendarNavController, calendarViewModel)*/ }
    }

    val shape by rememberBottomSheetShape(bottomSheetNavigator.navigatorSheetState)
    ModalBottomSheetLayout(
        bottomSheetNavigator,
        sheetShape = shape
    ) {
        Scaffold(
            bottomBar = { bottomAppBar() },
            floatingActionButton = { fab() },
            isFloatingActionButtonDocked = true,
            floatingActionButtonPosition = FabPosition.Center
        ) { Box(Modifier.padding(it)) {
            mainContent()
            Scrim(expandFabMenu) { expandFabMenu = false }
            fabMenu(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp)
            )
        } }
    }
}


