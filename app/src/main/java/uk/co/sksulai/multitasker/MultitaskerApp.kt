package uk.co.sksulai.multitasker

import javax.inject.Inject

import android.app.Application

import androidx.work.Configuration
import androidx.hilt.work.HiltWorkerFactory
import dagger.hilt.android.HiltAndroidApp

abstract class BaseMultitaskerApp : Application()
@HiltAndroidApp class MultitaskerApp : BaseMultitaskerApp(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
