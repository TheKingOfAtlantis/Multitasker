package uk.co.sksulai.multitasker.db.converter

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest

import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest class UriConverterTest : ConverterTest<Uri, String>(
    UriConverter,
    listOf(
        "http://localhost:8080",
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
