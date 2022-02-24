package uk.co.sksulai.multitasker.db.model

import java.time.*
import java.util.*
import androidx.room.*
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import uk.co.sksulai.multitasker.util.timezone

/**
 * Represents an event which takes place in a calendar
 *
 * At the core of the event two categories of attributes: Textual and Time.
 * Textual attributes are ultimately only present for the user, such as the
 * name and description as well as other attributes such as its category and
 * event tags.
 *
 * Date/time information, however, is required to correctly display when and how
 * long the event lasts. There are two possible ways to store and think about
 * this information both include a specific date/time + timezone to store the start
 * but differ in how we store/think about the end:
 *
 *  1) Duration from the start + timezone
 *  2) End as a separate date/time + timezone
 *
 * The first model is simpler as the duration is (usually) an invariant
 * property of an event, that regardless of the actual (start/end) timezone(s)
 * or how the start is shifted, the duration of the event remains the same. As
 * such we store this value and instead determine the end from this (+ timezone).
 *
 * @param eventID    Unique ID of this event
 * @param calendarID Calendar which contains this event
 * @param parentID   Parent event which this event is nested within
 *
 * @param name        Name of the event
 * @param description Description of the event
 * @param colour      Optional colour to associate with this event
 * @param category    Classification of the event
 * @param location    Location of the event
 *
 * @param allDay      Whether this event last all day
 * @param start       When this event starts
 * @param duration    How long this event lasts
 * @param endTimezone The timezone in which the end of the event occurs
 */
@Immutable @Parcelize
@Entity(
    tableName = "Event",
    foreignKeys = [
        ForeignKey(
            entity = CalendarModel::class,
            parentColumns = ["calendarID"],
            childColumns  = ["calendarID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EventModel::class,
            parentColumns = ["eventID"],
            childColumns  = ["parentID"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
) data class EventModel(
    @PrimaryKey val eventID: UUID,
    @ColumnInfo(index = true) val calendarID: UUID,
    @ColumnInfo(index = true) val parentID: UUID?,

    // Descriptors
    val name: String,
    val description: String,
    val category: String,
    val colour: Int?,
    val location: String,

    // Time Specifiers
    val allDay: Boolean,
    val start: ZonedDateTime,
    val duration: Duration,
    val endTimezone: TimeZone
) : Parcelable {
    /**
     * Represents the timezone in which the event starts
     */
    val startTimezone get() = start.timezone

    /**
     * Represents the time at which the event ends
     */
    // Combine the start + duration to get the end with respect to the local timezone
    // Then we adjust to the timezone to be what the user provided (with date/time adjusted as necessary)
    val end: ZonedDateTime get() = (start + duration).withZoneSameInstant(endTimezone.toZoneId())

    /**
     * [Color] object to be used to show the colour of [colour]
     */
    val uiColour: Color? get() = colour?.let { Color(it) }
    /**
     * Creates a copy of this [EventModel] with the given [colour]
     */
    fun withColor(colour: Color?) = copy(colour = colour?.toArgb())
}

/**
 * Represents a event tag which is used to quickly group and query for events
 *
 * @param tagID Unique ID of this tag
 * @param content Contents of the tag
 */
@Entity(tableName = "EventTag") data class EventTagModel(
    @PrimaryKey val tagID: UUID,
    @ColumnInfo(index = true) val content: String
)
/**
 * Junction table that associated [EventModel]s with [EventTagModel]s
 * @param eventID ID for an event
 * @param tagID   ID for the associated tag
 */
@Entity(
    primaryKeys = [ "tagID", "eventID" ],
    foreignKeys = [
        ForeignKey(
            entity = EventModel::class,
            parentColumns = ["eventID"],
            childColumns  = ["eventID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EventTagModel::class,
            parentColumns = ["tagID"],
            childColumns  = ["tagID"],
            onDelete = ForeignKey.CASCADE
        )
    ],
) data class EventTagJunction(
    @ColumnInfo(index = true) val eventID: UUID,
    @ColumnInfo(index = true) val tagID: UUID,
)

/**
 * Represents a list of tags of a given event
 * @param event The event that was queried
 * @param tags  The list of tags associated with this event
 */
data class EventWithTags(
    @Embedded val event: EventModel,
    @Relation(
        parentColumn = "eventID",
        entityColumn = "tagID",
        associateBy  = Junction(EventTagJunction::class)
    ) val tags: List<EventTagModel>
)
/**
 * Represents a list of events with a given tag
 * @param tag The tag which was queried
 * @param events List of events with the given tag
 */
data class EventsWithTag(
    @Embedded val tag: EventTagModel,
    @Relation(
        parentColumn = "tagID",
        entityColumn = "eventID",
        associateBy  = Junction(EventTagJunction::class)
    ) val events: List<EventModel>
)

/**
 * Represents an event with its associated calendar
 * @param event The event
 * @param calendar The calendar which is associated with the event
 */
data class EventWithCalendar(
    @Relation(
        parentColumn = "calendarID",
        entityColumn = "calendarID"
    ) val calendar: CalendarModel,
    @Embedded val event: EventModel
)
/**
 * Represents a parent event and its children
 * @param parent The parent event
 * @param children List of the parents children
 */
data class EventWithChildren(
    @Embedded val parent: EventModel,
    @Relation(
        parentColumn = "eventID",
        entityColumn = "parentID"
    ) val children: List<EventModel>
)
