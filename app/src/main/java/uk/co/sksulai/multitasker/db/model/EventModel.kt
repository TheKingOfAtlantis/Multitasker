package uk.co.sksulai.multitasker.db.model

import java.time.*
import java.util.*
import androidx.room.*
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 *
 *
 * @param eventID    Unique ID of this event
 * @param calendarID Calendar which contains this event
 * @param parentID   Parent event which this event is nested within
 *
 * @param name        Name of the event
 * @param description Description of the event
 * @param colour      Optional colour to associate with this event
 * @param category    Classification of the event
 * @param tags        Tags which the user can use to quickly search for related events
 *
 * @param allDay   Whether this event last all day
 * @param start    When this event starts
 * @param duration How long this event lasts
 */
@Immutable @Parcelize
@Entity(tableName = "Event") data class EventModel(
    @PrimaryKey val eventID: UUID,
    val calendarID: UUID,
    val parentID: UUID?,

    // Descriptors
    val name: String,
    val description: String,
    val colour: Int?,
    val category: String,
    val tags: String,

    // Time Specifiers
    val allDay: Boolean,
    val start: OffsetDateTime,
    val duration: Duration
) : Parcelable {
    /**
     * Represents the time at which the event ends i.e. [start] + [duration]
     */
    val end: OffsetDateTime get() = start + duration

    /**
     * [Color] object to be used to show the colour of [colour]
     */
    val uiColour: Color? get() = colour?.let { Color(it) }
    /**
     * Creates a copy of this [EventModel] with the given [colour]
     */
    fun withColor(colour: Color?) = copy(colour = colour?.toArgb())
}
