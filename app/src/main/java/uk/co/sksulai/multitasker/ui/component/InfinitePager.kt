package uk.co.sksulai.multitasker.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.*

/**
 * Total number of actual pages in the pager
 */
private const val totalPages   = Int.MAX_VALUE

/**
 * The initial index to use with the pager
 */
private const val initialIndex = totalPages/2

/**
 * Used to control and observe the state of an infinite pager
 */
@ExperimentalPagerApi
class InfinitePagerState constructor(
    private val pagerState: PagerState
) {
    /**
     * Retrieves the index of the current selected page.
     *
     * Pages are indexed relative to the initial page - pages before it are negative
     * and those after it are positive (referred to as infinite page index).
     *
     * @see PagerState.currentPage
     */
    val currentPage get() = pagerState.currentPage - initialIndex
    /**
     * Retrieves the index of the target page for on-going animations/scrolling.
     * If not animation in progress then is the same as [currentPage]
     *
     * Pages are indexed relative to the initial page - pages before it are negative
     * and those after it are positive (referred to as infinite page index).
     *
     * @see PagerState.targetPage
     */
    val targetPage get() = pagerState.targetPage  - initialIndex

    /**
     * Retrieves the internal [PagerState].
     * Should only to be used to read properties not exposed by [InfinitePagerState]
     * and should not be used to scroll
     */
    val internalState get() = pagerState

    /**
     * Smoothly animates scrolling to a given page
     *
     * @param page The infinite page index to scroll to
     * @param pageOffset Percentage of page width to offset (value in range 0f..1f)
     */
    suspend fun animateScrollToPage(
        page: Int,
        pageOffset: Float = 0f
    ) = pagerState.animateScrollToPage(page + initialIndex, pageOffset)
    /**
     * Immediately scrolls to the given page
     *
     * @param page The infinite page index to scroll to
     * @param pageOffset Percentage of page width to offset (value in range 0f..1f)
     */
    suspend fun scrollToPage(
        page: Int,
        pageOffset: Float = 0f
    ) = pagerState.scrollToPage(page + initialIndex, pageOffset)
}

/**
 * Smoothly animate scrolling to the next page. _N.B. This method does not
 * check or prevent the index being out of range if called on the last page_
 *
 * @param pageOffset Percentage of page width to offset (value in range 0f..1f)
 */
@ExperimentalPagerApi suspend fun InfinitePagerState.animateScrollToNextPage(
    pageOffset: Float = 0f
) = animateScrollToPage(currentPage + 1, pageOffset)
/**
 * Smoothly animate scrolling to the previous page. _N.B. This method does not
 * check or prevent the index being out of range if called on the last page_
 *
 * @param pageOffset Percentage of page width to offset (value in range 0f..1f)
 */
@ExperimentalPagerApi suspend fun InfinitePagerState.animateScrollToPreviousPage(
    pageOffset: Float = 0f
) = animateScrollToPage(currentPage - 1, pageOffset)

/**
 * Creates and remembers a [InfinitePagerState] used to control and observe a infinite pager
 */
@ExperimentalPagerApi
@Composable fun rememberInfinitePagerState(): InfinitePagerState {
    return InfinitePagerState(rememberPagerState(initialPage = initialIndex))
}

/**
 * Creates a horizontal pager with "infinite" pages
 *
 * @param modifier Modifier to pass to the horizontal pager
 * @param state    State object to access and control the pager
 * @param content  Used to define the contents of each page given the infinite page index
 */
@ExperimentalPagerApi
@Composable fun InfiniteHorizontalPager(
    modifier: Modifier = Modifier,
    state: InfinitePagerState = rememberInfinitePagerState(),
    content: @Composable PagerScope.(page: Int) -> Unit
) {
    HorizontalPager(
        modifier = modifier,
        count    = totalPages,
        state    = state.internalState,
    ) { content(it - initialIndex) }
}


/**
 * Creates a vertical pager with "infinite" pages
 *
 * @param modifier Modifier to pass to the horizontal pager
 * @param state    State object to access and control the pager
 * @param content  Used to define the contents of each page given the infinite page index
 */
@ExperimentalPagerApi
@Composable fun InfiniteVerticalPager(
    modifier: Modifier = Modifier,
    state: InfinitePagerState = rememberInfinitePagerState(),
    content: @Composable PagerScope.(page: Int) -> Unit
) {
    VerticalPager(
        modifier = modifier,
        count    = totalPages,
        state    = state.internalState,
    ) { content(it - initialIndex) }
}


