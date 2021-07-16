package uk.co.sksulai.multitasker.ui

import kotlin.math.*

import androidx.compose.ui.*
import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController

import androidx.navigation.compose.*

@Composable fun OnBoardingScreen(navController: NavHostController) {
    val state = rememberLazyListState()

    LazyRow(
        Modifier.fillMaxSize(),
        state,
        flingBehavior = object : FlingBehavior {
            override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                // Scroll to next/last depending on sign of velocity
                var nextIndex = state.firstVisibleItemIndex + initialVelocity.sign.toInt() * 1

                // Correct for potential overzealous flinging
                if(nextIndex < 0) nextIndex = 0
                if(nextIndex > state.layoutInfo.totalItemsCount - 1)
                    nextIndex = state.layoutInfo.totalItemsCount

                // Scroll to the page
                state.scrollToItem(nextIndex)

                return 0f // Consume all of the velocity
            }
        }
    ) {
        //TODO: Replace with actual onboarding pages
        item {
            Column(
                Modifier
                    .fillParentMaxSize()
                    .background(Color.Blue)
            ) {

            }
        }
        item {
            Column(
                Modifier
                    .fillParentMaxSize()
                    .background(Color.Green)
            ) {

            }
        }
        item {
            Column(
                Modifier
                    .fillParentMaxSize()
                    .background(Color.Red)
            ) {

            }
        }
    }

    // Lazy list doesn't perform any form of snapping
    // So we need to do that ourself

    // First grab hold of current interaction(s)
    // If we have just been released
    // Then check where we are
    //     if < 1/2 size of the page => Move to next
    //     else move back to the last

    val dragged by state.interactionSource.collectIsDraggedAsState()
    LaunchedEffect(dragged) {
        if(dragged) // Stop if currently dragged
            return@LaunchedEffect

        // Size of the first visible item
        val index = state.firstVisibleItemIndex
        val size  = state.layoutInfo.visibleItemsInfo.find {
            it.index == index
        }!!.size

        // Offset of the first page which is currently visible
        // How much of the page is currently not visible
        val offset = state.firstVisibleItemScrollOffset

        // If more of the page is not visible than is then animate to next
        if(offset > size * .5)
            state.animateScrollToItem(index + 1)
        else state.animateScrollToItem(index)
    }
}
