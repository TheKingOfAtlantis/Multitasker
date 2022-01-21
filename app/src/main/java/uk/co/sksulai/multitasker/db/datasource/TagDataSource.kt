package uk.co.sksulai.multitasker.db.datasource

import java.util.*
import kotlinx.coroutines.flow.Flow
import uk.co.sksulai.multitasker.db.model.*

interface TagDataSource {
    fun insert(vararg tag: EventTagModel)
    fun update(vararg tag: EventTagModel)
    fun delete(vararg tag: EventTagModel)

    fun associate(event: EventModel, tag: EventTagModel)
    fun associate(event: EventModel, tags: List<EventTagModel>)

    fun getAllTags(): Flow<List<EventTagModel>>

    fun fromID(id: UUID): Flow<EventTagModel?>
    fun fromName(content: String): Flow<List<EventTagModel>>

    fun forEvent(eventID: UUID): Flow<EventWithTags?>
    fun withTag(tagID: UUID): Flow<EventsWithTag>
}
