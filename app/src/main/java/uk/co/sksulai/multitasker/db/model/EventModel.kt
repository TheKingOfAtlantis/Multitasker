package uk.co.sksulai.multitasker.db.model

import java.time.*
import java.util.*
import androidx.room.*
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 *
 * @param ID         Unique ID of this event
 * @param CalendarID Calendar which contains this event
 * @param ParentID   Parent event which this event is nested within
 *
 * @param Name        Name of the event
 * @param Description Description of the event
 * @param Colour      Optional colour to associate with this event
 * @param Category    Classification of the event
 * @param Tags        Tags which the user can use to quickly search for related events
 *
 * @param AllDay   Whether this event last all day
 * @param Start    When this event starts
 * @param Duration How long this event lasts
 */
@Immutable @Parcelize
@Entity(tableName = "Event") data class EventModel(
    @PrimaryKey val ID: UUID,
    val CalendarID: UUID,
    val ParentID: UUID?,

    // Descriptors
    val Name: String,
    val Description: String,
    val Colour: Int?,
    val Category: String,
    val Tags: String,

    // Time Specifiers
    val AllDay: Boolean,
    val Start: OffsetDateTime,
    val Duration: Duration
) : Parcelable

/**
 * [Color] object to be used to show the colour of [EventModel.Colour]
 */
val EventModel.UiColour: Color? get() = Colour?.let { Color(it) }

/**
 * Represents the time at which the event ends
 * i.e. [EventModel.Start] + [EventModel.Duration]
 */
val EventModel.End: OffsetDateTime get() = Start + Duration
