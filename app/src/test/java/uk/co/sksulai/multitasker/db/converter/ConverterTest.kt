package uk.co.sksulai.multitasker.db.converter

import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

abstract class ConverterTest<T, U>(
    protected val converter : IConverter<T?, U?>,
    protected val testData: List<Pair<T?, U?>>
) {
    open fun withNull() {
        Assert.assertThat(
            null,
            Matchers.equalTo(converter.from(null))
        )
        Assert.assertThat(
            null,
            Matchers.equalTo(converter.to(null))
        )
    }
    open fun validateConversion() {
        testData.forEach { (lhs, rhs) ->
            Assert.assertThat(rhs, Matchers.equalTo(
                converter.from(lhs)
            ))
            Assert.assertThat(lhs, Matchers.equalTo(
                converter.to(rhs)
            ))
        }
    }
    open fun inverse() {
        // Check that applying:
        //     from(to(x)) == x
        //     to(from(y)) == y
        testData.forEach { (lhs, rhs) ->
            Assert.assertThat(lhs?.toString(), Matchers.equalTo(
                converter.from(converter.to(rhs))?.toString()
            ))
            // Fixme: Find work around ?.toString()
            //        Without it values don't match even tho the values are equivalent
            //        but one is surrounded by <...> and the other "..."
            Assert.assertThat(rhs?.toString(), Matchers.equalTo(
                converter.to(converter.from(lhs))?.toString()
            ))
        }
    }
}