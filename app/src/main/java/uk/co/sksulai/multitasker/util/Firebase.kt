package uk.co.sksulai.multitasker.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.*
import com.google.firebase.ktx.Firebase

@Composable fun setScreen(name: String, className: String? = null) { remember(name) {
    Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
        param(FirebaseAnalytics.Param.SCREEN_NAME, name)
        param(FirebaseAnalytics.Param.SCREEN_CLASS, className ?: name)
    }
    name
} }
