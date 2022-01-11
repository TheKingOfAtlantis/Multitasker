package uk.co.sksulai.multitasker.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.ktx.storage

@InstallIn(
    SingletonComponent::class
) @Module object FirebaseModule {
    @Provides fun provideFirebaseAuth() = Firebase.auth
    @Provides fun provideFirestoreDB()  = Firebase.firestore
    @Provides fun provideFirebaseStorage() = Firebase.storage
}
