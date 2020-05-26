package uk.co.sksulai.multitasker.db.converter

import android.net.Uri
import androidx.room.TypeConverter

class UriConverter : IConverter<Uri?, String?> {
    @TypeConverter override fun from(value: Uri?): String? = if(value != null) value.toString() else null
    @TypeConverter override fun to(value: String?): Uri? = if(value != null) Uri.parse(value) else null
}
