package uk.co.sksulai.multitasker.db.model

import java.util.*
import androidx.room.*
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Represents a calendar which contains events
 *
 * @param calendarID  Unique ID to identify this calendar
 * @param ownerID     ID associated with the owner's [UserModel]
 * @param name        Name of this calendar
 * @param description Description of this calendar
 * @param colour      Colour used for colour coordination
 * @param visible     Whether or not this calendar is visible
 */
@Immutable @Parcelize
@Entity(
    tableName = "Calendar",
    foreignKeys = [
        ForeignKey(
            entity = UserModel::class,
            parentColumns = ["userID"],
            childColumns = ["ownerID"]
        )
    ]
) data class CalendarModel(
    @PrimaryKey val calendarID: UUID,
    @ColumnInfo(index = true)
    val ownerID: String,

    val name: String,
    val description: String,
    val colour: Int,

    val visible: Boolean
) : Parcelable {
    /**
     * [Color] object to be used to show the colour of [CalendarModel.colour]
     */
    val uiColour: Color get() = Color(colour)
    /**
     * Creates a copy of this [CalendarModel] with the given [colour]
     */
    fun withColor(colour: Color) = copy(colour = colour.toArgb())
}
