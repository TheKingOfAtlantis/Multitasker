package uk.co.sksulai.multitasker.db.converter

import com.google.common.truth.Truth.assertThat

abstract class ConverterTest<T, U>(
    protected val converter : IConverter<T?, U?>,
    protected val testData: List<Pair<T?, U?>>
) {
    open fun withNull() {
        assertThat(converter.from(null))
            .isEqualTo(null)
        assertThat(converter.to(null))
            .isEqualTo(null)
    }
    open fun validateConversion() {
        testData.forEach { (lhs, rhs) ->
            assertThat(converter.from(lhs))
                .isEqualTo(rhs)
            assertThat(converter.to(rhs))
                .isEqualTo(lhs)
        }
    }
    open fun inverse() {
        // Check that applying:
        //     from(to(x)) == x
        //     to(from(y)) == y
        testData.forEach { (lhs, rhs) ->
            assertThat(converter.from(converter.to(rhs))?.toString())
                .isEqualTo(lhs?.toString())
            // Fixme: Find work around ?.toString()
            //        Without it values don't match even tho the values are equivalent
            //        but one is surrounded by <...> and the other "..."
            assertThat(converter.to(converter.from(lhs))?.toString())
                .isEqualTo(rhs?.toString())
        }
    }
}
