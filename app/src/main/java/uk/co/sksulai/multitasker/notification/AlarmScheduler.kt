package uk.co.sksulai.multitasker.notification

import java.time.Instant
import kotlin.reflect.KClass

import android.app.*
import android.content.*
import androidx.core.content.getSystemService
import androidx.work.ListenableWorker

/**
 * The alarm scheduler is used to create, update and delete alarms which are used trigger
 * [BroadcastReceiver]s at precise times. These receivers can retrieve the alarm id which
 * triggered them by using [Intent.getExtras] to determine the precise task they need to
 * execute
 *
 * @param receiver Class of the receiver to schedule
 * @param applicationContext Context used to access the alarm manager
 */
class AlarmScheduler <T : BroadcastReceiver> constructor(
    private val receiver: KClass<T>,
    val applicationContext: Context
) {
    private val alarmManager get() = applicationContext.getSystemService<AlarmManager>()!!

    companion object {
        const val AlarmID = "AlarmID"
    }

    /**
     * Used to construct the [PendingIntent] which is used by the [AlarmManager]
     *
     * @param alarmID ID of the alarm to be constructed
     * @param action  Routine to run using the [PendingIntent] which is constructed
     */
    private fun alarmAction(
        alarmID: Int,
        action: (PendingIntent) -> Unit
    ) {
        val intent = Intent(
            applicationContext,
            receiver.java
        ).apply { putExtra(AlarmID, alarmID) }

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            alarmID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        action(pendingIntent)
    }

    /**
     * Creates an alarm to trigger the notification receiver at [time] providing
     * it with [alarmID]
     */
    fun create(alarmID: Int, time: Instant) = alarmAction(alarmID) {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time.toEpochMilli(),
            it
        )
    }

    /**
     * Updates the alarm associated with [alarmID] to instead be triggered
     * at a newly provided [time]
     */
    fun update(alarmID: Int, time: Instant) {
        cancel(alarmID)
        create(alarmID, time)
    }

    /**
     * Cancels the alarm associated with [alarmID] stopping to from eventually
     * being triggered
     */
    fun cancel(alarmID: Int) = alarmAction(alarmID) {
        alarmManager.cancel(it)
    }
}

/**
 * The alarm scheduler is used to create, update and delete alarms which are used trigger
 * [BroadcastReceiver]s at precise times. These receivers can retrieve the alarm id which
 * triggered them by using [Intent.getExtras] to determine the precise task they need to
 * execute
 *
 * @param Receiver A [BroadcastReceiver] to be triggered by the alarm
 * @param context  Context used to access the alarm manager
 */
inline fun <reified Receiver : BroadcastReceiver> AlarmScheduler(context: Context) =
    AlarmScheduler(Receiver::class, context)

/**
 * The alarm scheduler is used to create, update and delete alarms which are used trigger
 * [BroadcastReceiver]s at precise times. These receivers can retrieve the alarm id which
 * triggered them by using [Intent.getExtras] to determine the precise task they need to
 * execute
 *
 * @param Receiver A [BroadcastReceiver] to be triggered by the alarm
 */
inline fun <reified Receiver : BroadcastReceiver> ListenableWorker.AlarmScheduler() =
    AlarmScheduler<Receiver>(applicationContext)


