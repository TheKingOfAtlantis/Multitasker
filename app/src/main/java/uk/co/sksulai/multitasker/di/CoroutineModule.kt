package uk.co.sksulai.multitasker.di

import javax.inject.Qualifier
import javax.inject.Singleton
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher

@Retention(AnnotationRetention.BINARY) @Qualifier annotation class DispatcherMain
@Retention(AnnotationRetention.BINARY) @Qualifier annotation class DispatcherDefault
@Retention(AnnotationRetention.BINARY) @Qualifier annotation class DispatcherUnconfined
@Retention(AnnotationRetention.BINARY) @Qualifier annotation class DispatcherIO

@InstallIn(SingletonComponent::class)
@Module object CoroutineModule {
    @Provides @Singleton @DispatcherDefault fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
    @Provides @Singleton @DispatcherMain fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
    @Provides @Singleton @DispatcherIO fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO
    @Provides @Singleton @DispatcherUnconfined fun provideUnconfinedDispatcher(): CoroutineDispatcher = Dispatchers.Unconfined
}
