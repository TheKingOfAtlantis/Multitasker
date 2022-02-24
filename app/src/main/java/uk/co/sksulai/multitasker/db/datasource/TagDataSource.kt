package uk.co.sksulai.multitasker.db.datasource

import java.util.*
import kotlinx.coroutines.flow.Flow
import uk.co.sksulai.multitasker.db.model.*

interface TagDataSource {
    /**
     * Insert a event tag
     */
    suspend fun insert(vararg tag: EventTagModel)
    /**
     * Update an event tag
     */
    suspend fun update(vararg tag: EventTagModel)
    /**
     * Deletes an event tag
     */
    suspend fun delete(vararg tag: EventTagModel)

    /**
     * Creates a link between an [event] and a [tag]
     * @param event Event to add the tag to
     * @param tag   Tag to associate with the event
     */
    suspend fun associate(event: EventModel, tag: EventTagModel)
    /**
     * Creates a link between an [event] and a list of tags
     * @param event Event to add the tags to
     * @param tags  List of tags to associate with the event
     */
    suspend fun associate(event: EventModel, tags: List<EventTagModel>)
    /**
     * Removes the link between an [event] and a list of tags
     * @param event Event to remove the tags to
     * @param tags  List of tags to disassociate from the event
     */
    suspend fun disassociate(event: EventModel, tags: List<EventTagModel>)

    /**
     * List of all the tags
     */
    fun getAll(): Flow<List<EventTagModel>>

    /**
     * Get an event tag from its [id]
     */
    fun fromID(id: UUID): Flow<EventTagModel?>
    /**
     * Get the list of event tags which whose content match [content]
     */
    fun fromContent(content: String): Flow<List<EventTagModel>>
    /**
     * Get the list of event tags which whose content is contained in [content]
     * @param content List of tag values to find
     */
    fun fromContent(content: List<String>): Flow<List<EventTagModel>>

    /**
     * Gets the [EventTagModel]s for a given [EventModel]
     * @param id ID of the event
     * @return [EventWithTags] containing the event and the
     */
    fun forEvent(id: UUID): Flow<EventWithTags?>
    /**
     * Gets a list of [EventModel] with a given [EventTagModel]
     * @param id ID of the tag
     * @return [EventWithTags] containing the tag and the events with the tag
     */
    fun withTag(id: UUID): Flow<EventsWithTag?>
}
