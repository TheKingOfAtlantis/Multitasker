package uk.co.sksulai.multitasker.di

import javax.inject.Singleton
import dagger.Module
import dagger.Provides
import dagger.hilt.testing.TestInstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

import android.content.Context

import uk.co.sksulai.multitasker.db.LocalDB
import uk.co.sksulai.multitasker.db.createDatabase

@TestInstallIn(
    components = [SingletonComponent::class],
    replaces   = [DatabaseModule::class]
) @Module object FakeDatabaseModule {
    @Provides @Singleton fun provideDatabase(
        @ApplicationContext context: Context
    ) = LocalDB.createDatabase(context, inMemory = true)
}
