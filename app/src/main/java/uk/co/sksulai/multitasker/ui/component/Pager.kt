package uk.co.sksulai.multitasker.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.*
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
