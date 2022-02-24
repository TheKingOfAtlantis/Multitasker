package uk.co.sksulai.multitasker.notification

import android.content.Context
import androidx.core.app.NotificationManagerCompat

fun getNotificationManager(context: Context) =
    NotificationManagerCompat.from(context)

