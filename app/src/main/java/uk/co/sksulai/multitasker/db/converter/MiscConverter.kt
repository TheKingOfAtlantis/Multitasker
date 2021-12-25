package uk.co.sksulai.multitasker.db.converter

import android.net.Uri
import androidx.room.TypeConverter

object UriConverter : IConverter<Uri?, String?> {
    @TypeConverter override fun from(value: Uri?): String? = value?.toString()
    @TypeConverter override fun to(value: String?): Uri?   = value?.let { Uri.parse(it) }
}
