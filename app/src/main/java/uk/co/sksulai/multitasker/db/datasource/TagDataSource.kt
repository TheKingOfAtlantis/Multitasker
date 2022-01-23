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
    fun disassociate(event: EventModel, tags: List<EventTagModel>)

    fun getAll(): Flow<List<EventTagModel>>

    fun fromID(id: UUID): Flow<EventTagModel?>
    fun fromContent(content: String): Flow<List<EventTagModel>>
    fun fromContent(content: List<String>): Flow<List<EventTagModel>>

    fun forEvent(id: UUID): Flow<EventWithTags?>
    fun withTag(id: UUID): Flow<EventsWithTag?>
}
