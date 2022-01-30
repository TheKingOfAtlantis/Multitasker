package uk.co.sksulai.multitasker.ui.screen.calendar.views

import java.time.*
import java.time.format.*
import kotlin.time.ExperimentalTime
import kotlin.time.Duration.Companion.minutes

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset

import androidx.hilt.navigation.compose.hiltViewModel

import uk.co.sksulai.multitasker.db.viewmodel.CalendarViewModel
import uk.co.sksulai.multitasker.util.rememberSaveableMutableState

@OptIn(ExperimentalTime::class)
@Composable fun DayView(
    currentDate: LocalDate,
    calendarViewModel: CalendarViewModel = hiltViewModel()
) {
    val zoom by rememberSaveableMutableState(1f)

    /**
     * A state which keeps track of the current date
     * We use this to ensure that the current time indicator is visible even if the
     * date changes
     */
    val today by produceState(LocalDate.now(), currentDate) {
        while(coroutineContext.isActive) {
            // Work out how long until the start of the next day
            // Keep the thread waiting until we change day
            // Set the current value to the new date
            val wait = Duration.between(LocalTime.now(), LocalDate.now().plusDays(1).atStartOfDay())
            delay(wait.toMillis())
            value = LocalDate.now()
        }
    }
    /**
     * A state which keeps track of the current time of day
     * We use this to position the current time of day indicator
     */
    // TODO: Smoothly animate the value
    val currentTime by produceState(LocalTime.now()) {
        while(coroutineContext.isActive) {
            // Update the current time every 5 minutes
            delay(5.minutes)
            value = LocalTime.now()
        }
    }

    Layout(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
        ,
        content = {
            val onBackground = MaterialTheme.colors.onBackground

            // Margin divider
            Box(
                Modifier
                    .layoutId("margin")
                    .fillMaxHeight()
                    .drawBehind {
                        drawLine(
                            onBackground,
                            start = Offset.Zero,
                            end = Offset(0f, size.height)
                        )
                    }
            )

            // Time of day dividers & labels
            (1..23).forEach {
                Box(
                    Modifier
                        .layoutId("time-divider-$it")
                        .fillMaxWidth()
                        .drawBehind {
                            drawLine(
                                onBackground,
                                start = Offset.Zero,
                                end = Offset(size.width, 0f)
                            )
                        }
                )
                Text(
                    modifier = Modifier.layoutId("time-label-$it"),
                    text = DateTimeFormatter
                        .ofLocalizedTime(FormatStyle.SHORT)
                        .format(LocalTime.of(it, 0, 0))
                )
            }

            // Current time of day indicator
            Box(
                Modifier
                    .layoutId("time-divider-now")
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            Color.Red,
                            start = Offset.Zero,
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            )
            Text(
                modifier = Modifier.layoutId("time-label-now"),
                text = DateTimeFormatter
                    .ofLocalizedTime(FormatStyle.SHORT)
                    .format(currentTime)
            )
        },
        measurePolicy = { measurables, constraints ->
            val relaxed = constraints.copy(minWidth = 0, minHeight = 0)

            val hourHeight = 64.dp * zoom

            val height = hourHeight * 24
            val width  = constraints.maxWidth

            val marginPlaceable = measurables
                .first { it.layoutId == "margin" }
                .measure(relaxed.copy(maxHeight = height.roundToPx()))

            val timeLabelPlaceables = (1..23)
                .map { time -> measurables.first { it.layoutId == "time-label-$time" } }
                .map { it.measure(relaxed) }
            val marginWidth = timeLabelPlaceables.maxOf { it.width } + 32.dp.roundToPx()
            val timeDividerPlaceables = (1..23)
                .map { time -> measurables.first { it.layoutId == "time-divider-$time" } }
                .map { it.measure(constraints.offset(horizontal = -marginWidth)) }

            // Current Time Indicator
            val nowLabelPlaceable = measurables
                .first { it.layoutId == "time-label-now" }
                .measure(relaxed)
            val nowDividerPlacable = measurables
                .first { it.layoutId == "time-divider-now" }
                .measure(constraints.offset(horizontal = -marginWidth))

            layout(width, height.roundToPx()) {
                marginPlaceable.placeRelative(marginWidth, 0)

                timeLabelPlaceables.fold(hourHeight) { offset, placeable ->
                    placeable.placeRelative(
                        (marginWidth - placeable.width)/2,
                        offset.roundToPx() - placeable.height/2
                    )
                    offset + hourHeight
                }
                timeDividerPlaceables.fold(hourHeight) { offset, placeable ->
                    placeable.placeRelative(marginWidth, offset.roundToPx())
                    offset + hourHeight
                }

                if(currentDate == today) {
                    val nowOffset = height * currentTime.toSecondOfDay().toFloat() / (24 * 60 * 60)
                    nowDividerPlacable.placeRelative(marginWidth, nowOffset.roundToPx())
                }
            }
        }
    )
}
