package uk.co.sksulai.multitasker.db.dao

import java.util.*
import androidx.room.*
import kotlinx.coroutines.flow.Flow

import uk.co.sksulai.multitasker.db.model.*
import uk.co.sksulai.multitasker.db.datasource.NotificationRuleDataSource

@Dao abstract class NotificationRuleDao : NotificationRuleDataSource, DatabaseService {
    @Insert abstract override suspend fun insert(vararg rule: NotificationRuleModel)
    @Update abstract override suspend fun update(vararg rule: NotificationRuleModel)
    @Delete abstract override suspend fun delete(vararg rule: NotificationRuleModel)

    @Insert protected abstract fun associate(junction: EventNotificationJunction)
    @Insert protected abstract fun associate(junction: CalendarNotificationJunction)

    @Transaction override suspend fun associate(
        calendar: CalendarModel,
        vararg rule: NotificationRuleModel
    ) = rule.forEach {
        associate(
            CalendarNotificationJunction(
                calendar.calendarID,
                it.notificationID
            )
        )
    }
    @Transaction override suspend fun associate(
        event: EventModel,
        vararg rule: NotificationRuleModel
    )  = rule.forEach {
        associate(
            EventNotificationJunction(
                event.eventID,
                it.notificationID
            )
        )
    }

    @Transaction override suspend fun override(
        event: EventModel,
        rule: NotificationRuleModel,
        overrides: NotificationOverride
    ) = associate(
        EventNotificationJunction(
            event.eventID,
            rule.notificationID,
            overrides
        )
    )

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH) // Use expandProjection so not actually an issue
    @MapInfo(keyColumn = "notificationID")
    @Query("Select * From EventNotificationJunction Where eventID == :eventID")
    abstract override fun overridesOf(eventID: UUID): Flow<Map<UUID, NotificationOverride>>

    @Query("Select * From NotificationRule Where notificationID is :id")
    abstract override fun fromID(id: UUID): Flow<NotificationRuleModel?>
    @Query("Select * From Calendar Where calendarID is :id")
    @Transaction abstract override fun fromCalendar(id: UUID): Flow<CalendarWithNotifications?>
    @Query("Select * From Event Where eventID is :id")
    @Transaction abstract override fun fromEvent(id: UUID): Flow<EventWithNotifications?>
}
