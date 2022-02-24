package uk.co.sksulai.multitasker.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.*
import uk.co.sksulai.multitasker.util.provideInScope
import uk.co.sksulai.multitasker.util.rememberMutableState

/**
 * Contents of each page
 */
@OptIn(ExperimentalPagerApi::class)
data class PageContent(
    val content: @Composable PagerScope.() -> Unit
)

/**
 * Defines the DSL for building pages
 */
@ExperimentalPagerApi
interface PagerBuilder {
    /**
     * Adds a page to the pager
     * @param content Contents of the page
     */
    fun page(content: @Composable PagerScope.() -> Unit)
}

@ExperimentalPagerApi
class PagerBuilderImpl : PagerBuilder {
    private val pages: MutableList<PageContent> = mutableListOf()

    val size: Int get() = pages.size
    operator fun get(index: Int) = pages[index]

    override fun page(content: @Composable PagerScope.() -> Unit) { pages += PageContent(content) }
}

/**
 * Creates a series of pages each presenting a different item from a list
 *
 * @param values List of values to use
 * @param content Composable defining the content using the members of the list
 */
@ExperimentalPagerApi
fun <T> PagerBuilder.page(values: List<T>, content: @Composable PagerScope.(T) -> Unit) =
    values.forEach { value -> page { content(value) } }
/**
 * Creates a series of pages each presenting a different item from an array
 *
 * @param values List of values to use
 * @param content Composable defining the content using the members of the array
 */
@ExperimentalPagerApi
fun <T> PagerBuilder.page(values: Array<T>, content: @Composable PagerScope.(T) -> Unit) =
    values.forEach { value -> page { content(value) } }

/**
 * Horizontal scrolling layout that allows the user to flip between items left and right.
 *
 * This version allows the use of [PagerBuilder] DSL to create pages rather than
 * using the index value to determine which page to create. Pages are created
 * using the [PagerBuilder.page] method, pages are then displayed in the order
 * in which they appear.
 */
@ExperimentalPagerApi
@Composable fun HorizontalPager(
    modifier: Modifier = Modifier,
    state: PagerState = rememberPagerState(),
    content: PagerBuilder.() -> Unit,
) {
    val pager = PagerBuilderImpl().apply(content)
    HorizontalPager(
        modifier = modifier,
        count    = pager.size,
        state    = state,
    ) { pager[it].content(this) }
}

/**
 * Vertically scrolling layout that allows the user to flip between items up and down.
 *
 * This version allows the use of [PagerBuilder] DSL to create pages rather than
 * using the index value to determine which page to create. Pages are created
 * using the [PagerBuilder.page] method, pages are then displayed in the order
 * in which they appear.
 */
@ExperimentalPagerApi
@Composable fun VerticalPager(
    modifier: Modifier = Modifier,
    state: PagerState = rememberPagerState(),
    content: PagerBuilder.() -> Unit,
) {
    val pager = PagerBuilderImpl().apply(content)
    VerticalPager(
        modifier = modifier,
        count    = pager.size,
        state    = state,
    ) { pager[it].content(this) }
}

/**
 * Smoothly animate scrolling to the next page. _N.B. This method does not
 * check or prevent the index being out of range if called on the last page_
 *
 * @param pageOffset Percentage of page width to offset (value in range 0f..1f)
 */
@ExperimentalPagerApi suspend fun PagerState.animateScrollToNextPage(
    pageOffset: Float = 0f
) = animateScrollToPage(currentPage + 1, pageOffset)
/**
 * Smoothly animate scrolling to the previous page. _N.B. This method does not
 * check or prevent the index being out of range if called on the last page_
 *
 * @param pageOffset Percentage of page width to offset (value in range 0f..1f)
 */
@ExperimentalPagerApi suspend fun PagerState.animateScrollToPreviousPage(
    pageOffset: Float = 0f
) = animateScrollToPage(currentPage - 1, pageOffset)

/**
 * Overlay to show over the contents of a pager which enable the user to navigate via button
 * presses (which can be helpful for those with restricted mobility which would make swiping
 * more difficult).
 *
 * Based on the state of the pager different UI elements will be exposed to correctly display
 * the set of available navigation options to the user
 *
 * @param pagerState The pager state to observe and control
 * @param modifier   The modifier to apply to this layout
 */
@ExperimentalPagerApi
@Composable fun HorizontalPagerDecoration(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    val nextPage = provideInScope(scope) { pagerState.animateScrollToNextPage() }
    val prevPage = provideInScope(scope) { pagerState.animateScrollToPreviousPage() }
    val lastPage = provideInScope(scope) { pagerState.animateScrollToPage(pagerState.pageCount - 1)  }

    HorizontalPagerDecoration(
        modifier   = modifier.fillMaxSize(),
        indicator  = { HorizontalPagerIndicator(pagerState = pagerState) },
        onLastPage = lastPage.takeIf { pagerState.targetPage != pagerState.pageCount - 1 },
        onNextPage = nextPage.takeIf { pagerState.targetPage != pagerState.pageCount - 1 },
        onPrevPage = prevPage.takeIf { pagerState.targetPage != 0 },
    )
}

/**
 * Overlay to show over the contents of a pager which enable the user to navigate via button
 * presses (which can be helpful for those with restricted mobility which would make swiping
 * more difficult).
 *
 * The overlay providers several slots in which the relevant UI element to navigate/scroll
 * the pager all of which are entirely optional (by specifying as null).
 *
 * @param indicator  Used to indicate which page in the pager the user in on
 * @param onNextPage Navigates to the next page. If null the associated button is hidden (animated)
 * @param onPrevPage Navigates to the previous page. If null the associated button is hidden (animated)
 * @param onLastPage Navigates to the last page (i.e. to the page at [PagerState.pageCount] - 1).
 *                   If null the associated button is hidden (animated)
 * @param modifier   Modifier to apply on this layout
 */
@ExperimentalPagerApi
@Composable fun HorizontalPagerDecoration(
    indicator: @Composable (() -> Unit)? = null,
    onNextPage: (() -> Unit)? = null,
    onPrevPage: (() -> Unit)? = null,
    onLastPage: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) = Box(modifier) {
    if(indicator != null) Box(
        Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 16.dp),
    ) { indicator() }

    // TODO: Replace with string resource
    AnimatedVisibility(
        modifier = Modifier.align(Alignment.TopEnd),
        visible  = onLastPage != null,
        enter    = fadeIn() + expandIn(),
        exit     = shrinkOut() + fadeOut()
    ) {
        TextButton(
            onClick = { onLastPage?.invoke() },
            content = { Text("Skip") }
        )
    }
    AnimatedVisibility(
        modifier = Modifier.align(Alignment.BottomEnd),
        visible  = onNextPage != null,
        enter    = fadeIn() + expandIn(),
        exit     = shrinkOut() + fadeOut()
    ) {
        TextButton(
            onClick = { onNextPage?.invoke() },
            content = { Text("Next") }
        )
    }
    AnimatedVisibility(
        modifier = Modifier.align(Alignment.BottomStart),
        visible  = onPrevPage != null,
        enter    = fadeIn() + expandIn(),
        exit     = shrinkOut() + fadeOut()
    ) {
        TextButton(
            onClick = { onPrevPage?.invoke() },
            content = { Text("Prev") }
        )
    }
}
