package uk.co.sksulai.multitasker.di

import dagger.Module
import dagger.Provides
import dagger.hilt.testing.TestInstallIn
import dagger.hilt.components.SingletonComponent

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

import uk.co.sksulai.multitasker.util.FirebaseEmulatorUtil
import javax.inject.Singleton


@TestInstallIn(
    components = [SingletonComponent::class],
    replaces   = [FirebaseModule::class]
) @Module object FakeFirebaseModule {
    private val emulatorSet = mutableMapOf<String, Boolean>()

    @Provides fun provideFirebaseAuth() = Firebase.auth.apply {
        if(emulatorSet["auth"] != true) {
            useEmulator(
                FirebaseEmulatorUtil.ip,
                FirebaseEmulatorUtil.port.auth
            )
            emulatorSet["auth"] = true
        }
    }
    @Provides fun provideFirestoreDB() = Firebase.firestore.apply {
        if(emulatorSet["db"] != true) {
            useEmulator(
                FirebaseEmulatorUtil.ip,
                FirebaseEmulatorUtil.port.db
            )
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build()
            emulatorSet["db"] = true
        }
    }
    @Provides fun provideFirebaseStorage() = Firebase.storage.apply {
        if(emulatorSet["storage"] != true) {
            useEmulator(
                FirebaseEmulatorUtil.ip,
                FirebaseEmulatorUtil.port.storage
            )
            emulatorSet["storage"] = true
        }
    }
}
