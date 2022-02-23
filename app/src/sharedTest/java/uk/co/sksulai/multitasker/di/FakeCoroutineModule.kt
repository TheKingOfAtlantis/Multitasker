package uk.co.sksulai.multitasker.di

import javax.inject.Singleton
import dagger.Module
import dagger.Provides
import dagger.hilt.testing.TestInstallIn
import dagger.hilt.components.SingletonComponent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher

@TestInstallIn(
    components = [SingletonComponent::class],
    replaces   = [CoroutineModule::class]
)@Module object FakeCoroutineModule {
    @Provides @Singleton @DispatcherDefault fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
    @Provides @Singleton @DispatcherMain fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
    @Provides @Singleton @DispatcherUnconfined fun provideUnconfinedDispatcher(): CoroutineDispatcher = Dispatchers.Unconfined
    @Provides @Singleton @DispatcherIO fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO
}
