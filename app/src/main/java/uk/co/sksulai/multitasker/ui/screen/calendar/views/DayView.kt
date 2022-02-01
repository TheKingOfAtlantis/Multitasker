package uk.co.sksulai.multitasker.ui.screen.calendar.views

import java.time.*
import java.time.format.*
import kotlin.time.ExperimentalTime
import kotlin.time.Duration.Companion.minutes
import kotlin.math.*

import kotlinx.coroutines.*

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.*
import androidx.compose.material.*
import androidx.compose.ui.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.input.pointer.util.*
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*

import androidx.hilt.navigation.compose.hiltViewModel

import uk.co.sksulai.multitasker.db.viewmodel.CalendarViewModel
import uk.co.sksulai.multitasker.util.rememberSaveableMutableState

@OptIn(ExperimentalTime::class)
@Composable fun DayView(
    currentDate: LocalDate,
    calendarViewModel: CalendarViewModel = hiltViewModel()
) {
    /**
     * The current amount to expand/contract the separation between time intervals
     */
    var zoom by rememberSaveableMutableState(1f)

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
            val wait = Duration.between(LocalDate.now().atTime(LocalTime.now()), LocalDate.now().plusDays(1).atStartOfDay())
            delay(wait.toMillis())
            value = LocalDate.now()
        }
    }
    /**
     * A state which keeps track of the current time of day
     * We use this to position the current time of day indicator
     */
    // TODO: Smoothly animate the value
    val currentTime by run {
        val time by produceState(LocalTime.now()) {
            while (coroutineContext.isActive) {
                // Update the current time every 5 minutes
                delay(5.minutes)
                value = LocalTime.now()
            }
        }
        animateValueAsState(
            targetValue = time,
            typeConverter = TwoWayConverter(
                convertToVector = { AnimationVector(it.toSecondOfDay().toFloat()) },
                convertFromVector = { LocalTime.ofSecondOfDay(it.value.toLong()) },
            ),
            animationSpec = tween(1000 /* 1s */),
        )
    }
    val scope = rememberCoroutineScope()

    val hourHeight = 64.dp * zoom

    val scrollState = rememberScrollState(with(LocalDensity.current) {
        // Calculate the position of the current time
        // Offset it by 1-2hrs
        // Convert to position in px
        val hourPos   = hourHeight * (currentTime.toSecondOfDay().toFloat()/(60 * 60))
        val offsetPos = (hourPos - hourHeight * 2f)
        offsetPos.coerceAtLeast(0.dp).roundToPx()
    })
    val nestedScrollConnection = remember { object : NestedScrollConnection { } }
    val nestedScrollDispatcher = remember { NestedScrollDispatcher() }

    Layout(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .nestedScroll(nestedScrollConnection, nestedScrollDispatcher)
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoomChange, _ ->
                    // To keep the centroid stationary we need to keep the relative position
                    // stationary before and after the transformation. However, the distance
                    // to the top (need to scroll the list correctly) is constant regardless
                    // of the actual zoom (screen doesn't actually get bigger)

                    val offset = Offset.Zero.copy(y = scrollState.value.toFloat()) - run {
                        // Z2 = Z1 * zoomChange
                        // C1/Z1 = C2/Z2 => C2 = C1 * Z2/Z1 => C2 = C1 * zoomChange
                        val topOffset   = centroid - Offset.Zero.copy(y = scrollState.value.toFloat())
                        val newCentroid = centroid * zoomChange
                        val top = newCentroid - topOffset
                        top - pan
                    }

                    // If zoomChange is shrink & zoom not too small
                    // If zoomChange is grow   & zoom not too large
                    if ((zoomChange < 1 && zoom > .75) || (zoomChange > 1 && zoom < 4))
                        zoom *= zoomChange
                    nestedScrollDispatcher.dispatchPostScroll(
                        consumed  = Offset.Zero,
                        available = offset,
                        source    = NestedScrollSource.Drag
                    )
                }
            }
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
            val nowDividerPlaceable = measurables
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
                    nowDividerPlaceable.placeRelative(marginWidth, nowOffset.roundToPx())
                }
            }
        }
    )
}
