package uk.co.sksulai.multitasker.db.converter

import android.net.Uri
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UriConverterTest : ConverterTest<Uri, String>(
    UriConverter(),
    listOf(
        "https://www.google.com",
        "ftp://www.example.com/api/user?id=5a5A81DF",
        "https://api.openweathermap.org/data/2.5/weather?q=London",
        "file://localhost/etc/fstab",
        "mailto:John.Doe@example.com",
        "news:comp.infosystems.www.servers.unix"
    ).let { str -> str.map{ Uri.parse(it).normalizeScheme()!! }.zip(str) }
) {
    @Test override fun withNull() = super.withNull()
    @Test override fun validateConversion() = super.validateConversion()
    @Test override fun inverse() = super.inverse()
}
