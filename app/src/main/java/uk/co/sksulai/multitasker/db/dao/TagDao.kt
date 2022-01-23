package uk.co.sksulai.multitasker.db.dao

import java.util.*
import kotlinx.coroutines.flow.Flow

import androidx.room.*

import uk.co.sksulai.multitasker.db.model.*
import uk.co.sksulai.multitasker.db.datasource.TagDataSource

@Dao abstract class TagDao : TagDataSource {
    @Insert abstract override fun insert(vararg tag: EventTagModel)
    @Update abstract override fun update(vararg tag: EventTagModel)
    @Delete abstract override fun delete(vararg tag: EventTagModel)

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
    @Insert protected abstract fun insert(vararg junction: EventTagJunction)
    /**
     * Deletes an entry in the junction table
     */
    @Delete protected abstract fun delete(vararg junction: EventTagJunction)
    /**
     * Get entries in the junction table
     */
    @Query("Select * From EventTagJunction Where tagID == :id")
    protected abstract fun junctionFromID(vararg id: UUID): List<EventTagJunction>

    override fun associate(event: EventModel, tag: EventTagModel) {
        insert(EventTagJunction(event.eventID, tag.tagID))
    }
    @Transaction override fun associate(event: EventModel, tags: List<EventTagModel>) {
        tags.map { EventTagJunction(event.eventID, it.tagID) }
            .toTypedArray()
            .let(this::insert)
    }
    @Transaction override fun disassociate(event: EventModel, tags: List<EventTagModel>) {
        tags.map { EventTagJunction(event.eventID, it.tagID) }
            .toTypedArray()
            .let(this::delete)
    }
}
