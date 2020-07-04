package uk.co.sksulai.multitasker.util

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
suspend fun Query.getAwait() = get().await()
suspend fun DocumentReference.getAwait() = get().await()
