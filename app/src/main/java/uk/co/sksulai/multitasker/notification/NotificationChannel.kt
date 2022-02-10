package uk.co.sksulai.multitasker.notification

import java.util.*

import android.app.*
import android.content.Context
import android.os.Build

import uk.co.sksulai.multitasker.db.model.CalendarModel
import uk.co.sksulai.multitasker.db.model.EventModel

object Notification {
    sealed class Group(
        val id: String,
        val name: String,
        val description: String
    ) {
        open fun create(context: Context) {
            val group = NotificationChannelGroup(id, name).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    this.description = description
            }
            getNotificationManager(context)
                .createNotificationChannelGroup(group)
        }

        object Calendar : Group(
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
        val group: Group? = null,
    ) {
        fun create(context: Context) {
            val channel = NotificationChannel(
                id,
                name,
                importance.value
            ).apply {
                description = this@Channel.description
                this@Channel.group?.id?.let { group = it }
                lockscreenVisibility = visibility.level
                if (vibration.isNotEmpty())
                    enableVibration(true)
                vibrationPattern = vibration
            }
            getNotificationManager(context)
                .createNotificationChannel(channel)
        }

        class Calendar(calendar: CalendarModel) : Channel(
            group = Group.Calendar,
            id = idOf(calendar),
            name = calendar.name,
            description = "Enabled/Disable notification from the ${calendar.name} calendar",
            importance = ChannelImportance.High,
        ) {
            companion object {
                fun idOf(id: UUID) = "calendar/$id"
                fun idOf(calendar: CalendarModel) = idOf(calendar.calendarID)
                fun idOf(event: EventModel) = idOf(event.calendarID)
            }
        }
    }
}
