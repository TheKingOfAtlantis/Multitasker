package uk.co.sksulai.multitasker.di

import javax.inject.Singleton
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

import uk.co.sksulai.multitasker.db.web.UserWebService

@InstallIn(
    SingletonComponent::class
) @Module object WebServiceModule {
}
