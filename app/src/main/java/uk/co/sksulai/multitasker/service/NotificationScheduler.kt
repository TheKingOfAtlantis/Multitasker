package uk.co.sksulai.multitasker.service

import java.util.*
import java.time.*

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

import javax.inject.Inject
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi

import android.app.*
import android.content.*
import android.text.format.DateUtils

import androidx.work.*
import androidx.hilt.work.HiltWorker

import uk.co.sksulai.multitasker.R
import uk.co.sksulai.multitasker.db.model.*
import uk.co.sksulai.multitasker.db.repo.*
import uk.co.sksulai.multitasker.notification.*
import uk.co.sksulai.multitasker.notification.Notification

@HiltWorker class NotificationScheduler @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val calendarRepo: CalendarRepo,
    private val schedulerRepo: SchedulerRepo
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

    /**
     * Ensures that changes made to a calendar regarding it's notification rules are
     * reflected in the scheduling of upcoming event notifications (if affected).
     */
    private suspend fun handleCalendar(id: String) = calendarRepo.withTransaction {
        calendarRepo.getCalendarFrom(UUID.fromString(id)).first()?.let {
            handleCalendar(it)
        } ?: let {
            // If null then the calendar was deleted
            // We could either delete everything or just let the alarm happen and deal
            // with the even not existing then

            // Probably best to deal with it now rather than potentially waking (and wasting battery)
            // just to find out all the event no longer exists
            // TODO: Work out a better way to do this
            schedulerRepo.getAll().first().filter {
                // rational: not checking event since some notification ids cover multiple events
                //          (and not the other way round)
                calendarRepo.getNotificationRuleFrom(it.notificationID).first() == null
            }
            .onEach { alarmScheduler.cancel(it.alarmID) }
            .let {schedulerRepo.delete(*it.toTypedArray()) }
        }
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun handleCalendar(calendar: CalendarModel) = calendarRepo.withTransaction {
        val events = calendarRepo.getEventFrom(calendar).flatMapLatest {
            combine(
                it.map { event ->
                    calendarRepo.determineNotificationsOf(event)
                        .map { EventWithNotifications(event, it) }
                }
            ) { it.toList() }
        }
        handleEvents(events.first())
    }

    private suspend fun handleEvent(id: String) = calendarRepo.withTransaction {
        calendarRepo.getEventFrom(UUID.fromString(id)).first()?.let {
            handleEvent(it)
        } ?: let {
            // If null then the calendar was deleted
            // Unlike with the calendar we know precisely who to remove
            // So we retrieve all schedule entries for this event
            //   => Cancel the alarms
            //   => Remove from the database

            schedulerRepo.fromEvent(UUID.fromString(id)).first()
                .onEach { alarmScheduler.cancel(it.alarmID) }
                .let { schedulerRepo.delete(*it.toTypedArray()) }
        }
    }
    private suspend fun handleEvent(event: EventModel) = calendarRepo.withTransaction {
        val events = calendarRepo.determineNotificationsOf(event)
            .map { EventWithNotifications(event, it) }
            .first()
        handleEvents(listOf(events))
    }


    /**
     * General handler for a list of events to ensure proper scheduling regardless of
     * if has already been scheduled, the schedule needs to be modified or if nothing
     * needs to be done
     */
    private suspend fun handleEvents(events: List<EventWithNotifications>) = calendarRepo.withTransaction {
        events.forEach { (event, rules) ->
            val existing = schedulerRepo.fromEvent(event).first()
            if(existing.isNotEmpty()) {
                // If we've already got the event scheduled need to do a couple things:
                //  1) Check if any new notifications have been added
                //         => Create new schedule entry
                //         => Post to alarm manager
                //  2) Check if any notifications have been removed
                //         => Remove schedule entry
                //         => Remove from alarm manager
                //  3) Check if the start of the event has changed
                //         => Update schedule entries
                //         => Update alarm manager
                //  4) Check if any of the notification rules have changed
                //         => Update schedule entries
                //         => Update alarm manager

                // 1)
                rules.filter { it.notificationID !in existing.map { it.notificationID } }
                    .mapNotNull { schedulerRepo.createEventNotification(event, it) }
                    .forEach { alarmScheduler.create(it.alarmID, it.scheduledTime) }

                // 2)
                existing.filter { it.notificationID !in rules.map { it.notificationID } }
                    .onEach { alarmScheduler.cancel(it.alarmID) }
                    .let { schedulerRepo.delete(*it.toTypedArray()) }

                // 3 + 4)
                existing
                    .filter { schedule ->
                        val eventChanged = schedule.start != event.start
                        val notificationChange = rules
                            .find { schedule.notificationID == it.notificationID  }
                            ?.let { schedule.reminder != it.duration }

                        eventChanged || (notificationChange ?: true)
                    }
                    .associateWith { schedulerRepo.update(it.alarmID) }
                    .forEach { (original, new) ->
                        if(new == null) // If the original was removed => cancel the alarm
                            alarmScheduler.cancel(original.alarmID)
                        else alarmScheduler.update(new.alarmID, new.scheduledTime)
                    }
            } else {
                // If instead no existing records exist
                //  1) Create schedule entry
                //  2) Post to alarm manager
                rules.mapNotNull { schedulerRepo.createEventNotification(event, it) }
                    .forEach { alarmScheduler.create(it.alarmID, it.scheduledTime) }
            }
        }
    }
    /**
     * This method handles the routine/period scheduling of notifications which
     * takes place once each day.
     * It handles cleaning up the table of posted notifications and works out
     * which events need scheduling and does the necessary work
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun handleRoutine() {
        schedulerRepo.withTransaction {
            // First prune the table
            // Then retrieve events which have yet to start
            // Determine when their reminders are
            // Schedule the reminders taking place in the next 24hrs
            schedulerRepo.prune()
            calendarRepo.getEventAfter(ZonedDateTime.now())
                .flatMapLatest {
                    it.associateWith(calendarRepo::determineNotificationsOf)
                      .map { (event, rules) -> rules.map { EventWithNotifications(event, it) } }
                      .let { combine(it) { it.toList() } }
                }
                .first()
                .forEach { (event, rules) ->
                    // Filter notifications where either it yet to be sent or was due some
                    // time in the past
                    rules.filter {
                        val time = event.start - it.duration
                        // Ensure the notification will take place some time between now
                        // and 24hrs from now
                        time.isAfter(ZonedDateTime.now()) &&
                        time.isBefore(ZonedDateTime.now().plusDays(1))
                    }.forEach {
                        // For those left lets create an entry in the schedule (if it already doesn't exist)
                        val schedule = schedulerRepo.createEventNotification(event, it)
                        if(schedule != null) // Not already scheduled so lets create it
                            alarmScheduler.create(schedule.alarmID, schedule.scheduledTime)
                    }
                }
            }
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

        when {
            inputData.hasKeyWithValueOfType<String>(DataType.CalendarID) ->
                handleCalendar(inputData.getString(DataType.CalendarID)!!)
            inputData.hasKeyWithValueOfType<String>(DataType.EventID) ->
                handleEvent(inputData.getString(DataType.EventID)!!)
            else -> handleRoutine()
        }

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
