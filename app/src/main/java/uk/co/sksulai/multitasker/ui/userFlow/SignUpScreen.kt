package uk.co.sksulai.multitasker.ui.userFlow

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.android.material.datepicker.CalendarConstraints

import kotlinx.coroutines.launch

import uk.co.sksulai.multitasker.db.viewmodel.UserViewModel
import uk.co.sksulai.multitasker.ui.TextDateConverter
import uk.co.sksulai.multitasker.ui.component.PagerBuilder
import uk.co.sksulai.multitasker.ui.component.Paging
import uk.co.sksulai.multitasker.util.LocalActivity
import uk.co.sksulai.multitasker.util.setScreen

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetTime
import java.time.ZoneOffset

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalPagerApi::class
) @Composable fun SignUpScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel(),
) = Scaffold { Box {
    val pagerState = rememberPagerState(pageCount = 2)

    HorizontalPager(state = pagerState, dragEnabled = false) { page -> Paging(page) {
        emailVerificationPage(pagerState, userViewModel)
        detailsPage(navController, pagerState, userViewModel)
    } }
} }

@ExperimentalPagerApi
fun PagerBuilder.emailVerificationPage(
    pagerState: PagerState,
    userViewModel: UserViewModel
) = page { Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {

    val emailVerification = userViewModel.emailVerification
    LaunchedEffect(Unit) {
        if(emailVerification.verified)
            pagerState.animateScrollToPage(pagerState.currentPage + 1)
        else {
            emailVerification.request("user/signup")
            setScreen("Email Verification")
        }
    }
    Text("Email verification request sent")
    Text("Check your email")
} }

@ExperimentalPagerApi
fun PagerBuilder.detailsPage(
    navController: NavController,
    pagerState: PagerState,
    userViewModel: UserViewModel
) = page { Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    setScreen("Sign Up Wizard")

    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()

    val currentUser by userViewModel.currentUser.collectAsState(initial = null)

    val displayName = rememberFieldState()
    val dob         = rememberFieldState()

    TextField(
        modifier = Modifier.padding(bottom = 8.dp),
        label = { Text("Your name") },
        value = displayName.text,
        onValueChange = displayName::onChange
    )

    Box(modifier = Modifier.padding(bottom = 8.dp)) {
        TextField(
            label = { Text("Date of Birth") },
            value = dob.text,
            onValueChange = dob::onChange,
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.EditCalendar, null) }
        )
        Box(Modifier.matchParentSize().clickable {
            val initial = dob.text.let { TextDateConverter.to(it) ?: LocalDate.now() }

            DatePicker.single(activity, "Date of Birth", initial,
                CalendarConstraints.Builder()
                    .setOpenAt(
                        initial.atTime(OffsetTime.of(0, 0, 0, 0, ZoneOffset.UTC))
                            .toInstant()
                            .toEpochMilli()
                    ).setEnd(
                        LocalDateTime.now()
                            .toInstant(ZoneOffset.UTC)
                            .toEpochMilli()
                    ).build()
            ) {
                dob.onChange(TextDateConverter.from(it))
            }
        })
    }
    Button(
        onClick = { scope.launch {
            userViewModel.update(
                currentUser!!.copy(
                    DisplayName = displayName.text,
                    DOB = dob.text.let {
                        it.takeIf { it.isNotEmpty() }
                            ?.let { TextDateConverter.to(it) }
                    }
                )
            )
            navController.navigate("CalendarView")
        } }
    ) { Text("Submit") }
} }

