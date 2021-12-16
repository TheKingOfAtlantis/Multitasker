package uk.co.sksulai.multitasker.db.dao

import org.junit.Test
import com.google.common.truth.Truth.assertThat

class LocalSearchQueryTest {
    @Test fun queryWithAnyStartOnly() {
        assertThat(SearchQuery.local("TestQuery") {
            anyStart = true
            anyEnd   = false
        }).isEqualTo("%TestQuery")
        assertThat(SearchQuery.local("TestQuery") {
            anyStart = true
        }).isEqualTo("%TestQuery")
    }
    @Test fun queryWithAnyEndOnly() {
        assertThat(SearchQuery.local("TestQuery") {
            anyStart = false
            anyEnd   = true
        }).isEqualTo("TestQuery%")
        assertThat(SearchQuery.local("TestQuery") {
            anyEnd = true
        }).isEqualTo("TestQuery%")
    }
    @Test fun queryWithAnyStartOrEnd() {
        assertThat(SearchQuery.local("TestQuery") {
            anyStart = true
            anyEnd   = true
        }).isEqualTo("%TestQuery%")
    }
    @Test fun queryExactly() {
        assertThat(SearchQuery.local("TestQuery") {
            anyStart = false
            anyEnd   = false
        }).isEqualTo("TestQuery")

        assertThat(SearchQuery.local("TestQuery"))
            .isEqualTo("TestQuery")
    }
}
