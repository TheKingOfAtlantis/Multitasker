package uk.co.sksulai.multitasker.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

import android.content.Context
import androidx.work.WorkManager

@InstallIn(
    SingletonComponent::class
) @Module object WorkManagerModule {
    @Provides fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager = WorkManager.getInstance(context)
}
