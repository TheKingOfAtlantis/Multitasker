package uk.co.sksulai.multitasker.di

import javax.inject.Singleton
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext

import android.content.Context

import uk.co.sksulai.multitasker.db.LocalDB
import uk.co.sksulai.multitasker.db.createDatabase

@InstallIn(
    SingletonComponent::class
) @Module object DatabaseModule {
    @Provides @Singleton fun provideDatabase(
        @ApplicationContext context: Context
    ) = LocalDB.createDatabase(context)
}

@InstallIn(
    SingletonComponent::class
) @Module object DaoModule {
    @Provides fun provideUserDao(db: LocalDB)         = db.userDao

    @Provides fun provideCalendarDao(db: LocalDB)     = db.calendarDao
    @Provides fun provideEventDao(db: LocalDB)        = db.eventDao
    @Provides fun provideTagDao(db: LocalDB)          = db.tagDao

    @Provides fun provideNotificationDao(db: LocalDB) = db.notificationRuleDao
    @Provides fun provideEventNotificationScheduleDao(db: LocalDB) = db.eventNotificationDao
}
