package uk.co.sksulai.multitasker

import java.time.Duration
import java.time.Instant

import android.content.Context
import android.app.Notification
import android.app.PendingIntent
import android.widget.RemoteViews

import androidx.core.app.NotificationCompat

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

enum class NotificationPriority(val level: Int) {
    /** @see NotificationCompat.PRIORITY_DEFAULT */
    Default(NotificationCompat.PRIORITY_DEFAULT),

    /** @see NotificationCompat.PRIORITY_MIN */
    Min(NotificationCompat.PRIORITY_MIN),
    /** @see NotificationCompat.PRIORITY_LOW */
    Low(NotificationCompat.PRIORITY_LOW),
    /** @see NotificationCompat.PRIORITY_HIGH */
    High(NotificationCompat.PRIORITY_HIGH),
    /** @see NotificationCompat.PRIORITY_MAX */
    Max(NotificationCompat.PRIORITY_MAX),
}
enum class NotificationVisibility(val level: Int) {
    /** @see NotificationCompat.VISIBILITY_PUBLIC */
    Public(NotificationCompat.VISIBILITY_PUBLIC),
    /** @see NotificationCompat.VISIBILITY_PRIVATE */
    Private(NotificationCompat.VISIBILITY_PRIVATE),
    /** @see NotificationCompat.VISIBILITY_SECRET */
    Secret(NotificationCompat.VISIBILITY_SECRET)
}
enum class NotificationCategory(val value: String) {
    /** @see NotificationCompat.CATEGORY_CALL */
    Call(NotificationCompat.CATEGORY_CALL),
    /** @see NotificationCompat.CATEGORY_NAVIGATION */
    Navigation(NotificationCompat.CATEGORY_NAVIGATION),
    /** @see NotificationCompat.CATEGORY_MESSAGE */
    Message(NotificationCompat.CATEGORY_MESSAGE),
    /** @see NotificationCompat.CATEGORY_EMAIL */
    Email(NotificationCompat.CATEGORY_EMAIL),
    /** @see NotificationCompat.CATEGORY_EVENT */
    Event(NotificationCompat.CATEGORY_EVENT),
    /** @see NotificationCompat.CATEGORY_PROMO */
    Promo(NotificationCompat.CATEGORY_PROMO),
    /** @see NotificationCompat.CATEGORY_ALARM */
    Alarm(NotificationCompat.CATEGORY_ALARM),
    /** @see NotificationCompat.CATEGORY_PROGRESS */
    Progress(NotificationCompat.CATEGORY_PROGRESS),
    /** @see NotificationCompat.CATEGORY_SOCIAL */
    Social(NotificationCompat.CATEGORY_SOCIAL),
    /** @see NotificationCompat.CATEGORY_ERROR */
    Error(NotificationCompat.CATEGORY_ERROR),
    /** @see NotificationCompat.CATEGORY_TRANSPORT */
    Transport(NotificationCompat.CATEGORY_TRANSPORT),
    /** @see NotificationCompat.CATEGORY_SYSTEM */
    System(NotificationCompat.CATEGORY_SYSTEM),
    /** @see NotificationCompat.CATEGORY_SERVICE */
    Service(NotificationCompat.CATEGORY_SERVICE),
    /** @see NotificationCompat.CATEGORY_REMINDER */
    Reminder(NotificationCompat.CATEGORY_REMINDER),
    /** @see NotificationCompat.CATEGORY_RECOMMENDATION */
    Recommendation(NotificationCompat.CATEGORY_RECOMMENDATION),
    /** @see NotificationCompat.CATEGORY_STATUS */
    Status(NotificationCompat.CATEGORY_STATUS),
    /** @see NotificationCompat.CATEGORY_WORKOUT */
    Workout(NotificationCompat.CATEGORY_WORKOUT),
    /** @see NotificationCompat.CATEGORY_LOCATION_SHARING */
    LocationSharing(NotificationCompat.CATEGORY_LOCATION_SHARING),
    /** @see NotificationCompat.CATEGORY_STOPWATCH */
    Stopwatch(NotificationCompat.CATEGORY_STOPWATCH),
    /** @see NotificationCompat.CATEGORY_MISSED_CALL */
    MissedCall(NotificationCompat.CATEGORY_MISSED_CALL),
}

abstract class NotificationBuilder(
    context: Context,
    channelID: String
) {
    protected val builder = NotificationCompat.Builder(context, channelID)

    /**
     * _Required_
     *
     * The small icon placed in the top corner of a notification
     *
     * Default: [R.drawable.ic_logo_small]
     *
     * @see [NotificationCompat.Builder.setSmallIcon]
     */
    @DrawableRes var smallIcon: Int = R.drawable.ic_logo_small
        set(value) {
            builder.setSmallIcon(value)
            field = value
        }

    /**
     * _Optional_
     *
     * A large icon placed in the notification
     *
     * Default: [R.drawable.ic_logo_small]
     *
     * @see [NotificationCompat.Builder.setLargeIcon]
     */
    var largeIcon: Bitmap? = null
        set(value) {
            builder.setLargeIcon(value)
            field = value
        }
    /**
     * _Required_
     *
     * Sets the priority level of a notification
     *
     * Default: [NotificationPriority.Default]
     *
     * @see [NotificationCompat.Builder.setPriority]
     */
    var priority: NotificationPriority = NotificationPriority.Default
        set(value) {
            builder.priority = value.level
            field = value
        }

    /**
     * _Required_
     *
     * Sets the visibility of a notification which may be:
     *  - Public:  Show this notification in its entirety on all lockscreens.
     *  - Private: Show this notification on all lockscreens, but conceal sensitive or
     *             private information on secure lockscreens.
     *  - Secret:  Do not reveal any part of this notification on a secure lockscreen.
     *
     * Default: [NotificationVisibility.Private]
     *
     * @see [NotificationCompat.Builder.setVisibility]
     */
    var visibility: NotificationVisibility = NotificationVisibility.Private
        set(value) {
            builder.setVisibility(value.level)
            field = value
        }

    /**
     * _Optional_
     *
     * The title (first row) of the notification
     *
     * @see [NotificationCompat.Builder.setContentTitle]
     */
    var title: String? = null
        set(value) {
            builder.setContentTitle(value)
            field = value
        }
    /**
     * _Optional_
     *
     * The main content (second row) of the notification
     *
     * @see [NotificationCompat.Builder.setContentText]
     */
    var content: String? = null
        set(value) {
            builder.setContentText(value)
            field = value
        }
    /**
     * _Optional_
     *
     * Additional text provided along with the title and content typically as a helper,
     * e.g. to indicate which account the notification is relevant to
     *
     * @see [NotificationCompat.Builder.setContentText]
     */
    var subtext: String? = null
        set(value) {
            builder.setSubText(value)
            field = value
        }

    /**
     * _Optional_
     *
     * Custom [RemoteViews] to use instead of standard one
     *
     * @see [NotificationCompat.Builder.setContent]
     */
    var contentView: RemoteViews? = null
        set(value) {
            builder.setContent(value)
            field = value
        }

    /**
     * _Optional_
     *
     * Intent which is launched when then notification is clicked by the user
     *
     * @see [NotificationCompat.Builder.setContentIntent]
     */
    var contentIntent: PendingIntent? = null
        set(value) {
            builder.setContentIntent(value)
            field = value
        }
    /**
     * _Optional_
     *
     * Intent which is launched when then notification is cleared
     *
     * @see [NotificationCompat.Builder.setDeleteIntent]
     */
    var onClearedIntent: PendingIntent? = null
        set(value) {
            builder.setDeleteIntent(value)
            field = value
        }

    /**
     * _Optional_
     *
     * Text that is displayed in the status bar when the notification
     * first arrives
     *
     * @see [NotificationCompat.Builder.setTicker]
     */
    var ticker: String? = null
        set(value) {
            builder.setTicker(value)
            field = value
        }
    /**
     * _Optional_
     *
     * Sets when the event triggering the notification occurred. For events/tasks/etc
     * this should be set to when it starts
     *
     * Default: [Instant.now()]
     *
     * @see [NotificationCompat.Builder.setWhen]
     */
    var `when`: Instant = Instant.now()
        set(value) {
            builder.setWhen(value.toEpochMilli())
            field = value
        }

    /**
     * _Optional_
     *
     * Control whether the timestamp set with [when] is shown
     *
     * @see [NotificationCompat.Builder.setShowWhen]
     */
    var showWhen: Boolean = true
        set(value) {
            builder.setShowWhen(value)
            field = value
        }
    private var _whenAsCountdown: Boolean = false

    /**
     * _Optional_
     *
     * Sets the chronometer to show [when] as a countdown. Instead of a
     * timestamp the minutes and seconds since [when] is shown instead.
     *
     * Useful for showing the elapsing time
     *
     * @see whenAsStopwatch
     * @see [NotificationCompat.Builder.setUsesChronometer]
     */
    var whenAsCountdown: Boolean
        get() = _whenAsCountdown
        set(value) {
            if(value) builder.setUsesChronometer(value)
            builder.setChronometerCountDown(value)

            _whenAsCountdown = value
            if(value) _whenAsStopwatch = false
        }
    private var _whenAsStopwatch: Boolean = false
    /**
     * _Optional_
     *
     * Sets the chronometer to show [when] as a stopwatch. Instead of a
     * timestamp the minutes and seconds counting down to [when] is
     * shown instead
     *
     * @see [NotificationCompat.Builder.setUsesChronometer]
     * @see [NotificationCompat.Builder.setChronometerCountDown]
     */
    var whenAsStopwatch: Boolean
        get() = _whenAsStopwatch
        set(value) {
            builder.setUsesChronometer(value)
            if(value) builder.setChronometerCountDown(false)

            _whenAsStopwatch = value
            // When this is true ensure as countdown if false
            if(value) _whenAsCountdown = false
        }

    /**
     * _Optional_
     *
     * Ongoing notifications cannot be dismissed by the user that typically used
     * to indicate that a background task is taking place
     *
     * @see [NotificationCompat.Builder.setOngoing]
     */
    var onGoing: Boolean = false
        set(value) {
            builder.setOngoing(value)
            field = value
        }
    /**
     * _Optional_
     *
     * Colour to be applied to the notification background. Setting this to null
     * will cause the notification to not be colourised
     *
     * @see [NotificationCompat.Builder.setColorized]
     * @see [NotificationCompat.Builder.setColor]
     */
    var colour: Color? = null
        set(value) {
            builder.setColorized(value != null)
            value?.let { builder.setColor(value.toArgb()) }
            field = value
        }

    /**
     * _Optional_
     *
     * Category which can be used by the system for ranking and filtering
     */
    var category: NotificationCategory? = null
        set(value) {
            value?.let(NotificationCategory::value)
                 ?.let(builder::setCategory)
            field = value
        }

    /**
     * _Optional_
     *
     * Used to order notifications from the same package/group
     */
    var sortKey: String? = null
        set(value) {
            builder.setSortKey(value)
            field = value
        }

    /**
     * _Optional_
     *
     * Used to create a notification action button
     *
     * @param icon   (Required) The icon for this action
     * @param title  (Optional) The title of this action
     * @param intent (Optional) [PendingIntent] to fire when this action is triggered
     *
     * @return The action which was created
     */
    fun action(
        @DrawableRes icon: Int,
        title: String? = null,
        intent: PendingIntent? = null,
        builder: NotificationCompat.Action.Builder.() -> Unit
    ) = NotificationCompat.Action.Builder(icon, title, intent)
            .apply(builder)
            .build()
            .also(this.builder::addAction)

    /**
     * _Optional_
     *
     * Sets a timeout after which the notification is cancelled
     */
    var timeout: Duration? = null
        set(value) {
            value?.toMillis()
                ?.let(builder::setTimeoutAfter)
            field = value
        }
    /**
     * _Optional_
     *
     * Sets the rich notification style to be apply
     */
    var style: NotificationCompat.Style? = null
        set(value) {
            value?.let(builder::setStyle)
            field = value
        }

    /**
     * Adds an indeterminate progress bar
     */
    fun progressBar() { builder.setProgress(0, 0, true) }
    /**
     * Adds an determinate progress bar
     *
     * @param current The current value
     * @param max     The maximum value
     */
    fun progressBar(current: Int, max: Int) { builder.setProgress(max, current, true) }

    /**
     * Ensures that this notification is silent.
     * If false it falls back to the sounds & vibrations of the channel
     */
    var silent: Boolean = false
        set(value) {
            builder.setSilent(value)
            field = value
        }
}

abstract class NotificationGroupBuilder(
    private val context: Context,
    private val group: String,
    private val channel: String
) {
    protected var notifications: MutableList<Notification> = mutableListOf()

    /**
     * Creates the group summary notification
     * @param channelID Optional channel ID override for the notification
     */
    fun summary(
        channelID: String = channel,
        builder: NotificationBuilder.() -> Unit
    ) = NotificationBuilderImpl(context, channelID)
        .apply(builder)
        .apply {
            groupID = group
            isGroupSummary = true
        }.build()
        .also { notifications += it }

    /**
     * Creates notification that is part of the group
     * @param channelID Optional channel ID override for the notification
     */
    fun notification(
        channelID: String = channel,
        builder: NotificationBuilder.() -> Unit
    ) = NotificationBuilderImpl(context, channelID)
        .apply(builder)
        .apply { groupID = group }
        .build()
        .also { notifications += it }
}

private class NotificationBuilderImpl(
    context: Context,
    channelID: String
) : NotificationBuilder(context, channelID) {
    /**
     * ID of the group to place the notification in
     * @see NotificationCompat.Builder.setGroup
     */
    var groupID: String? = null
        set(value) {
            if(value != null)
                builder.setGroup(groupID)
            field = value
        }
    /**
     * Whether or not a given notification is the group summary notification
     * @see NotificationCompat.Builder.setGroupSummary
     */
    var isGroupSummary: Boolean = false
        set(value) {
            builder.setGroupSummary(value)
            field = value
        }

    fun build() = builder.build()
}

private class NotificationGroupBuilderImpl(
    context: Context,
    groupID: String,
    channelID: String
) : NotificationGroupBuilder(context, groupID, channelID) {
    fun build() = notifications.toList()
}

/**
 * Creates a notification
 *
 * @param context [Context] used to create notifications
 * @param channel The notification channel ID
 * @param builder The notification builder DSL
 *
 * @return The notification which was created
 */
fun buildNotification(
    context: Context,
    channel: String,
    builder: NotificationBuilder.() -> Unit
) = NotificationBuilderImpl(context, channel)
    .apply(builder)
    .build()

/**
 * Creates a group of notifications
 *
 * @param context [Context] used to create notifications
 * @param group   The group ID for the notifications
 * @param channel The notification channel ID
 * @param builder The notification group builder DSL
 *
 * @return List of the notifications which were created
 */
fun buildNotificationGroup(
    context: Context,
    group: String,
    channel: String,
    builder: NotificationGroupBuilder.() -> Unit
) = NotificationGroupBuilderImpl(context, group, channel)
    .apply(builder)
    .build()

