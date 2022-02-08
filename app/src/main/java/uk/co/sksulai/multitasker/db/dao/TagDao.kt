package uk.co.sksulai.multitasker.db.dao

import java.util.*
import kotlinx.coroutines.flow.Flow

import androidx.room.*

import uk.co.sksulai.multitasker.db.model.*
import uk.co.sksulai.multitasker.db.datasource.TagDataSource

@Dao abstract class TagDao : TagDataSource, DatabaseService {
    @Insert abstract override suspend fun insert(vararg tag: EventTagModel)
    @Update abstract override suspend fun update(vararg tag: EventTagModel)
    @Delete abstract override suspend fun delete(vararg tag: EventTagModel)

    @Query("Select * From EventTag")
    abstract override fun getAll(): Flow<List<EventTagModel>>

    @Query("Select * From EventTag Where tagID like :id")
    abstract override fun fromID(id: UUID): Flow<EventTagModel?>
    @Query("Select * From EventTag Where content like :content")
    abstract override fun fromContent(content: String): Flow<List<EventTagModel>>
    @Query("Select * From EventTag Where content like (:content)")
    abstract override fun fromContent(content: List<String>): Flow<List<EventTagModel>>

    @Query("Select * From Event Where eventID == :id")
    @Transaction abstract override fun forEvent(id: UUID): Flow<EventWithTags?>
    @Query("Select * From EventTag Where tagID == :id")
    @Transaction abstract override fun withTag(id: UUID): Flow<EventsWithTag>

    // Event-Tag Junction Table
    /**
     * Inserts a entry to the junction table
     */
    @Insert protected abstract suspend fun insert(vararg junction: EventTagJunction)
    /**
     * Deletes an entry in the junction table
     */
    @Delete protected abstract suspend fun delete(vararg junction: EventTagJunction)
    /**
     * Get entries in the junction table
     */
    @Query("Select * From EventTagJunction Where tagID == :id")
    protected abstract fun junctionFromID(vararg id: UUID): Flow<List<EventTagJunction>>

    override suspend fun associate(event: EventModel, tag: EventTagModel) {
        insert(EventTagJunction(event.eventID, tag.tagID))
    }
    @Transaction override suspend fun associate(event: EventModel, tags: List<EventTagModel>) {
        tags.map { EventTagJunction(event.eventID, it.tagID) }
            .toTypedArray()
            .let { insert(*it) }
    }
    @Transaction override suspend fun disassociate(event: EventModel, tags: List<EventTagModel>) {
        tags.map { EventTagJunction(event.eventID, it.tagID) }
            .toTypedArray()
            .let { delete(*it) }
    }
}
