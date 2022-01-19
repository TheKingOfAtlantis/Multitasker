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
    @Provides fun provideUserDao(db: LocalDB)     = db.getUserDao()
    @Provides fun provideCalendarDao(db: LocalDB) = db.getCalendarDao()
    @Provides fun provideEventDao(db: LocalDB)    = db.getEventDao()
}
