package uk.co.sksulai.multitasker.db.converter

import org.junit.Test
import com.google.common.truth.Truth.assertThat

sealed class ConverterTest<T, U>(
    private val converter: IConverter<T?, U?>,
    private val testData: List<Pair<T?, U?>>
) {
    /**
     * @brief Checks that passing null to a converter returns null (in both directions)
     */
    @Test open fun withNull() {
        // Check that applying:
        //     from(null) == null
        //     to(null)   == null
        assertThat(converter.from(null)).isEqualTo(null)
        assertThat(converter.to(null)).isEqualTo(null)
    }

    /**
     * @brief Ensures conversions produce the correct output
     */
    @Test open fun validateConversion() {
        // Check that applying:
        //     from(x) == y
        //     to(y)   == x
        testData.forEach { (lhs, rhs) ->
            assertThat(converter.from(lhs)).isEqualTo(rhs)
            assertThat(converter.to(rhs)).isEqualTo(lhs)
        }
    }

    /**
     * @brief Ensures conversions are strongly reversible - i.e. that calls of
     *        to(...) followed by from(...), or vice versa, identically reproduce
     *        the original object's state
     */
    @Test open fun inverse() {
        // Check that applying:
        //     from(to(x)) == x
        //     to(from(y)) == y
        testData.forEach { (lhs, rhs) ->
            assertThat(converter.from(converter.to(rhs))).isEqualTo(rhs)
            assertThat(converter.to(converter.from(lhs))).isEqualTo(lhs)
        }
    }
}
