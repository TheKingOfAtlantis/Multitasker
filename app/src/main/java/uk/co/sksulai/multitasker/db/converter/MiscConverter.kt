package uk.co.sksulai.multitasker.db.converter

import androidx.room.TypeConverter

import java.util.*
import android.net.Uri

object UriConverter : IConverter<Uri?, String?> {
    @TypeConverter override fun from(value: Uri?): String? = value?.toString()
    @TypeConverter override fun to(value: String?): Uri?   = value?.let(Uri::parse)
}

object UUIDConverter : IConverter<UUID?, String?> {
    @TypeConverter override fun from(value: UUID?): String? = value?.toString()
    @TypeConverter override fun to(value: String?): UUID?   = value?.let(UUID::fromString)
}
