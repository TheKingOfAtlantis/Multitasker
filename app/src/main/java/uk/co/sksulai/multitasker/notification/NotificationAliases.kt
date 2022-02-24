package uk.co.sksulai.multitasker.notification

import android.app.NotificationManager
import androidx.core.app.NotificationCompat

enum class ChannelImportance(val value: Int) {
    Default(NotificationManager.IMPORTANCE_DEFAULT),
    Max(NotificationManager.IMPORTANCE_MAX),
    High(NotificationManager.IMPORTANCE_HIGH),
    Low(NotificationManager.IMPORTANCE_LOW),
    Min(NotificationManager.IMPORTANCE_MIN),
    None(NotificationManager.IMPORTANCE_NONE),
    Unspecified(NotificationManager.IMPORTANCE_UNSPECIFIED),
}

enum class NotificationPriority(val level: Int) {
    /** @see NotificationCompat.PRIORITY_DEFAULT */
    Default(NotificationCompat.PRIORITY_DEFAULT),

    /** @see NotificationCompat.PRIORITY_MIN */
    Min(NotificationCompat.PRIORITY_MIN),
    /** @see NotificationCompat.PRIORITY_LOW */
    Low(NotificationCompat.PRIORITY_LOW),
    /** @see NotificationCompat.PRIORITY_HIGH */
    High(NotificationCompat.PRIORITY_HIGH),
    /** @see NotificationCompat.PRIORITY_MAX */
    Max(NotificationCompat.PRIORITY_MAX),
}
enum class NotificationVisibility(val level: Int) {
    /** @see NotificationCompat.VISIBILITY_PUBLIC */
    Public(NotificationCompat.VISIBILITY_PUBLIC),
    /** @see NotificationCompat.VISIBILITY_PRIVATE */
    Private(NotificationCompat.VISIBILITY_PRIVATE),
    /** @see NotificationCompat.VISIBILITY_SECRET */
    Secret(NotificationCompat.VISIBILITY_SECRET)
}
enum class NotificationCategory(val value: String) {
    /** @see NotificationCompat.CATEGORY_CALL */
    Call(NotificationCompat.CATEGORY_CALL),
    /** @see NotificationCompat.CATEGORY_NAVIGATION */
    Navigation(NotificationCompat.CATEGORY_NAVIGATION),
    /** @see NotificationCompat.CATEGORY_MESSAGE */
    Message(NotificationCompat.CATEGORY_MESSAGE),
    /** @see NotificationCompat.CATEGORY_EMAIL */
    Email(NotificationCompat.CATEGORY_EMAIL),
    /** @see NotificationCompat.CATEGORY_EVENT */
    Event(NotificationCompat.CATEGORY_EVENT),
    /** @see NotificationCompat.CATEGORY_PROMO */
    Promo(NotificationCompat.CATEGORY_PROMO),
    /** @see NotificationCompat.CATEGORY_ALARM */
    Alarm(NotificationCompat.CATEGORY_ALARM),
    /** @see NotificationCompat.CATEGORY_PROGRESS */
    Progress(NotificationCompat.CATEGORY_PROGRESS),
    /** @see NotificationCompat.CATEGORY_SOCIAL */
    Social(NotificationCompat.CATEGORY_SOCIAL),
    /** @see NotificationCompat.CATEGORY_ERROR */
    Error(NotificationCompat.CATEGORY_ERROR),
    /** @see NotificationCompat.CATEGORY_TRANSPORT */
    Transport(NotificationCompat.CATEGORY_TRANSPORT),
    /** @see NotificationCompat.CATEGORY_SYSTEM */
    System(NotificationCompat.CATEGORY_SYSTEM),
    /** @see NotificationCompat.CATEGORY_SERVICE */
    Service(NotificationCompat.CATEGORY_SERVICE),
    /** @see NotificationCompat.CATEGORY_REMINDER */
    Reminder(NotificationCompat.CATEGORY_REMINDER),
    /** @see NotificationCompat.CATEGORY_RECOMMENDATION */
    Recommendation(NotificationCompat.CATEGORY_RECOMMENDATION),
    /** @see NotificationCompat.CATEGORY_STATUS */
    Status(NotificationCompat.CATEGORY_STATUS),
    /** @see NotificationCompat.CATEGORY_WORKOUT */
    Workout(NotificationCompat.CATEGORY_WORKOUT),
    /** @see NotificationCompat.CATEGORY_LOCATION_SHARING */
    LocationSharing(NotificationCompat.CATEGORY_LOCATION_SHARING),
    /** @see NotificationCompat.CATEGORY_STOPWATCH */
    Stopwatch(NotificationCompat.CATEGORY_STOPWATCH),
    /** @see NotificationCompat.CATEGORY_MISSED_CALL */
    MissedCall(NotificationCompat.CATEGORY_MISSED_CALL),
}
