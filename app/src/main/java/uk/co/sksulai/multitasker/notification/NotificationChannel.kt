package uk.co.sksulai.multitasker.notification

import java.util.*

import android.app.*
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat

import uk.co.sksulai.multitasker.db.model.CalendarModel
import uk.co.sksulai.multitasker.db.model.EventModel
import uk.co.sksulai.multitasker.ui.MultitaskerPalette

object Notification {
    sealed class ChannelGroup(
        val id: String,
        val name: String,
        val description: String
    ) {
        open fun create(context: Context) {
            getNotificationManager(context)
                .createNotificationChannelGroup(
                    NotificationChannelGroupCompat.Builder(id).apply {
                        setName(name)
                        setDescription(description)
                    }.build()
                )
        }

        object Calendar : ChannelGroup(
            "calendar",
            "Calendars",
            "Notifications from calendars"
        )
    }

    sealed class Channel(
        val id: String,
        val name: String,
        val description: String,
        val importance: ChannelImportance = ChannelImportance.Default,
        val visibility: NotificationVisibility = NotificationVisibility.Private,
        val vibration: LongArray = longArrayOf(1000, 1000),
        val group: ChannelGroup? = null,
        val light: Color = MultitaskerPalette.Primary,
        val showBadge: Boolean = true,
        val sound: Uri? = null,
        val audioAttributes: AudioAttributes? = null
    ) {
        /**
         * Used to create a channel
         */
        fun create(context: Context) = getNotificationManager(context)
                .createNotificationChannel(
                    NotificationChannelCompat.Builder(id, importance.value)
                        .setGroup(group?.id)
                        .setName(name)
                        .setDescription(description)
                        .setVibrationEnabled(vibration.isNotEmpty())
                        .setVibrationPattern(vibration.takeIf { it.isNotEmpty() })
                        .setLightsEnabled(light.isUnspecified)
                        .setLightColor(light.toArgb())
                        .setShowBadge(showBadge)
                        .setSound(sound, audioAttributes)
                        .build()
                )

        /**
         * Used to delete a channel
         */
        fun delete(context: Context) = getNotificationManager(context)
            .deleteNotificationChannel(id)

        class Calendar(calendar: CalendarModel) : Channel(
            group       = ChannelGroup.Calendar,
            id          = idOf(calendar),
            name        = calendar.name,
            description = "Enabled/Disable notification from the ${calendar.name} calendar",
            importance  = ChannelImportance.High,
        ) {
            companion object {
                fun idOf(id: UUID) = "calendar/$id"
                fun idOf(calendar: CalendarModel) = idOf(calendar.calendarID)
                fun idOf(event: EventModel) = idOf(event.calendarID)
            }
        }
    }
}
