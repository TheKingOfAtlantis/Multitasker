package uk.co.sksulai.multitasker.ui.screen.calendar

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import uk.co.sksulai.multitasker.ui.Destinations

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable fun CalendarScreen(
    navController: NavController
) {
    val bottomSheetNavigator  = rememberBottomSheetNavigator()
    val calendarNavController = rememberNavController(bottomSheetNavigator)

    ModalBottomSheetLayout(
        bottomSheetNavigator,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        NavHost(calendarNavController, startDestination = "start") {
            composable("start") { }
            CalendarCreation()
        }
    }
}

@ExperimentalMaterialNavigationApi
fun NavGraphBuilder.CalendarCreation() = bottomSheet(Destinations.CalendarCreation.route) {


}
