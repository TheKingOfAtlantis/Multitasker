package uk.co.sksulai.multitasker

import javax.inject.Inject

import android.app.Application

import androidx.work.Configuration
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import uk.co.sksulai.multitasker.notification.Notification
import uk.co.sksulai.multitasker.service.NotificationScheduler.Companion.startRoutineScheduling

abstract class BaseMultitaskerApp : Application()
@HiltAndroidApp class MultitaskerApp : BaseMultitaskerApp(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var workManager: WorkManager

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Register Notification groups + channels
        Notification.ChannelGroup.Calendar.create(applicationContext)

        // Trigger routine notification scheduling
        workManager.startRoutineScheduling()
    }
}
