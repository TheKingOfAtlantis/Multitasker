package uk.co.sksulai.multitasker.db.model

import java.util.*
import androidx.room.*
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 *
 * @param ID          Unique ID to identify this calendar
 * @param OwnerID     ID associated with the owner's [UserModel]
 * @param Name        Name of this calendar
 * @param Description Description of this calendar
 * @param Colour      Colour used for colour coordination
 * @param Visible     Whether or not this calendar is visible
 */
@Immutable @Parcelize
@Entity(tableName = "Calendar") data class CalendarModel(
    @PrimaryKey val ID: UUID,
    val OwnerID: String,

    val Name: String,
    val Description: String?,
    val Colour: Int,

    val Visible: Boolean
) : Parcelable

/**
 * [Color] object to be used to show the colour of [CalendarModel.Colour]
 */
val CalendarModel.UiColour: Color get() = Color(Colour)
/**
 * Creates a copy of this [CalendarModel] with the given [colour]
 */
fun CalendarModel.withColor(colour: Color) = copy(Colour = colour.toArgb())
