package uk.co.sksulai.multitasker.di

import dagger.Module
import dagger.Provides
import dagger.hilt.testing.TestInstallIn
import dagger.hilt.components.SingletonComponent

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

import uk.co.sksulai.multitasker.util.FirebaseEmulatorUtil
import javax.inject.Singleton


@TestInstallIn(
    components = [SingletonComponent::class],
    replaces   = [FirebaseModule::class]
) @Module object FakeFirebaseModule {
    private var authEmulatorSet = false
    @Provides fun provideFirebaseAuth() = Firebase.auth.apply {
        if(!authEmulatorSet) {
            useEmulator(
                FirebaseEmulatorUtil.ip,
                FirebaseEmulatorUtil.port.auth
            )
            authEmulatorSet = true
        }
    }
    private var dbEmulateSet = false
    @Provides fun provideFirestoreDB() = Firebase.firestore.apply {
        if(!dbEmulateSet) {
            useEmulator(
                FirebaseEmulatorUtil.ip,
                FirebaseEmulatorUtil.port.db
            )
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build()
            dbEmulateSet = true
        }
    }
}
