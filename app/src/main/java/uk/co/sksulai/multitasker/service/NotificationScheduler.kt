package uk.co.sksulai.multitasker.service

import javax.inject.Inject
import java.time.Duration

import androidx.work.*
import androidx.hilt.work.HiltWorker
import dagger.hilt.android.qualifiers.ApplicationContext

import uk.co.sksulai.multitasker.R
import uk.co.sksulai.multitasker.db.model.*
import uk.co.sksulai.multitasker.db.repo.*
import uk.co.sksulai.multitasker.notification.*
import uk.co.sksulai.multitasker.notification.Notification

@HiltWorker class NotificationScheduler @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {
    private val alarmScheduler = AlarmScheduler<EventNotificationReceiver>()

    object WorkerTags {
        const val Routine = "notification-scheduler-routine"
    }
    object DataType {
        const val CalendarID = "calendar"
        const val EventID = "event"
    }

    companion object {
        /**
         * Starts the routine notification scheduling worker
         *
         * This worker is used to ensure that the next 24hr of reminders/notifications
         * are scheduled so that they are shown to the user as and when they requested
         */
        fun WorkManager.startRoutineScheduling() = enqueueUniquePeriodicWork(
            WorkerTags.Routine,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<NotificationScheduler>(Duration.ofDays(1))
                .setInitialDelay(Duration.between(
                        LocalTime.now(),
                        LocalTime.MAX
                ))
                .addTag(WorkerTags.Routine)
                .build()
        )

        /**
         * Triggers one-off scheduling for the given [event] after it has been
         * created or updated to ensure changes are reflecting in the schedule
         * ASAP (e.g. if an event reminder on the same day as it was created)
         */
        fun WorkManager.startOneOffScheduling(event: EventModel) = beginUniqueWork(
            "schedule/event/${event.eventID}",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequest.Builder(NotificationScheduler::class.java)
                .setInputData(
                    Data.Builder()
                        .putString(DataType.EventID, event.eventID.toString())
                        .build()
                )
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
        )
        /**
         * Triggers one-off scheduling for the given [event] after it has been
         * updated to ensure changes to its reminders are reflected in the schedule
         */
        fun WorkManager.startOneOffScheduling(calendar: CalendarModel) = beginUniqueWork(
            "schedule/calendar/${calendar.calendarID}",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequest.Builder(NotificationScheduler::class.java)
                .setInputData(
                    Data.Builder()
                        .putString(DataType.CalendarID, calendar.calendarID.toString())
                        .build()
                )
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
        )
    }

    override suspend fun doWork(): Result {
        // Create a table in which we record the next 24hrs work of reminders
        //     It will contain a reference to both the event and the notification rule
        //     Use it to keep track of which reminders have already been scheduled, when
        //     they have been scheduled and the relevant identifiers need to modify the alarm
        //     that way if changes are made to the time we can retrieve the relevant ID
        //     of the alarm that we need to cancel or update

        // If we get given a calendar retrieve all of its events then work scheduling for each
        // If given an event then work out its schedule
        // If nothing given then must be the routine scheduler

        return Result.success()
    }
}

class EventNotificationReceiver @Inject constructor(
    private val schedulerRepo: SchedulerRepo,
    private val calendarRepo: CalendarRepo
) : AlarmBroadcastReceiver() {

    fun Context.makeEventNotification(
        event: EventModel
    ) {
        val notification = buildNotification(
            this,
            Notification.Channel.Calendar.idOf(event)
        ) {
            smallIcon = R.drawable.ic_logo_small
            title     = event.name
            content   = DateUtils.formatDateRange(
                this@makeEventNotification,
                event.start.toInstant().toEpochMilli(),
                event.end.toInstant().toEpochMilli(),
                DateUtils.FORMAT_ABBREV_ALL or
                        DateUtils.FORMAT_SHOW_TIME
            )

            `when`          = event.start.toInstant()
            whenAsStopwatch = true
            contentIntent   = null // TODO: Replace with intent to event viewer
        }
        getNotificationManager(this)
            .notify(event.eventID.hashCode(), notification)
    }

    override fun onReceive(context: Context, alarmID: Int) {
        runBlocking {
            // While we could assume that the events/schedule/etc all still exist
            // Safer to assume that they don't rather than sending a notification for something
            // that no longer exists
            val schedule = schedulerRepo.fromAlarmID(alarmID).first()
            if(schedule != null) {
                calendarRepo.getEventFrom(schedule.notificationID).first()?.let { event ->
                    context.makeEventNotification(event)
                    schedulerRepo.markPosted(schedule)
                }
            }
        }
    }
}
