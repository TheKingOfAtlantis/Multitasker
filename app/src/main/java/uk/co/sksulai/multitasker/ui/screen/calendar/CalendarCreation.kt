package uk.co.sksulai.multitasker.ui.screen.calendar

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.navigation.NavController

import uk.co.sksulai.multitasker.db.viewmodel.CalendarViewModel

@Composable fun CalendarCreation(
    navController: NavController,
    calendarViewModel: CalendarViewModel,
) = Column(
    Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
) {
}
