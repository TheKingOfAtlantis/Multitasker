package uk.co.sksulai.multitasker.db.converter

import org.hamcrest.Matchers
import org.junit.Assert

abstract class ConverterTest<T, U>(
    protected val converter : IConverter<T?, U?>,
    protected val testData: List<Pair<T?, U?>>
) {
    open fun withNull() {
        Assert.assertThat(
            converter.from(null),
            Matchers.equalTo<U?>(null)
        )
        Assert.assertThat(
            converter.to(null),
            Matchers.equalTo<T?>(null)
        )
    }
    open fun validateConversion() {
        testData.forEach { (lhs, rhs) ->
            Assert.assertThat(
                converter.from(lhs),
                Matchers.equalTo(rhs)
            )
            Assert.assertThat(
                converter.to(rhs),
                Matchers.equalTo(lhs)
            )
        }
    }
    open fun inverse() {
        // Check that applying:
        //     from(to(x)) == x
        //     to(from(y)) == y
        testData.forEach { (lhs, rhs) ->
            Assert.assertThat(
                converter.from(converter.to(rhs))?.toString(),
                Matchers.equalTo(lhs?.toString())
            )
            // Fixme: Find work around ?.toString()
            //        Without it values don't match even tho the values are equivalent
            //        but one is surrounded by <...> and the other "..."
            Assert.assertThat(
                converter.to(converter.from(lhs))?.toString(),
                Matchers.equalTo(rhs?.toString())
            )
        }
    }
}
