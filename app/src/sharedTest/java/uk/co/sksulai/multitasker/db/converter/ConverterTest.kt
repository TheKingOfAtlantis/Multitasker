package uk.co.sksulai.multitasker.db.converter

import org.junit.Test
import com.google.common.truth.Truth.assertThat

sealed class ConverterTest<T, U>(
    private val converter: IConverter<T?, U?>,
    private val testData: List<Pair<T?, U?>>
) {
    /**
     * Checks that passing null to a converter returns null (in both directions)
     */
    @Test open fun withNull() {
        // Check that applying:
        //     from(null) == null
        //     to(null)   == null
        withNull_From()
        withNull_To()
    }
    open fun withNull_From() = assertThat(converter.from(null)).isEqualTo(null)
    open fun withNull_To() = assertThat(converter.to(null)).isEqualTo(null)

    /**
     * Ensures conversions produce the correct output
     */
    @Test open fun validateConversion() {
        // Check that applying:
        //     from(x) == y
        //     to(y)   == x
        testData.forEach { (lhs, rhs) ->
            validateConversion_From(lhs, rhs)
            validateConversion_To(lhs, rhs)
        }
    }
    open fun validateConversion_From(lhs: T?, rhs: U?) = assertThat(converter.from(lhs)).isEqualTo(rhs)
    open fun validateConversion_To(lhs: T?, rhs: U?) = assertThat(converter.to(rhs)).isEqualTo(lhs)

    /**
     * Ensures conversions are strongly reversible - i.e. that calls of to(...)
     * followed by from(...), or vice versa, identically reproduce the original
     * object's state
     */
    @Test open fun inverse() {
        // Check that applying:
        //     from(to(x)) == x
        //     to(from(y)) == y
        testData.forEach { (lhs, rhs) ->
            inverse_FromThenTo(lhs, rhs)
            inverse_ToThenFrom(lhs, rhs)
        }
    }
    open fun inverse_FromThenTo(lhs: T?, rhs: U?) = assertThat(converter.from(converter.to(rhs))).isEqualTo(rhs)
    open fun inverse_ToThenFrom(lhs: T?, rhs: U?) = assertThat(converter.to(converter.from(lhs))).isEqualTo(lhs)
}
