package uk.co.sksulai.multitasker.db.dao

import java.util.*
import kotlinx.coroutines.flow.Flow

import androidx.room.*

import uk.co.sksulai.multitasker.db.model.*
import uk.co.sksulai.multitasker.db.datasource.TagDataSource

@Dao abstract class TagDao : TagDataSource {
    @Insert abstract override fun insert(vararg tag: EventTagModel)
    @Update abstract override fun update(vararg tag: EventTagModel)
    @Transaction override fun delete(vararg tag: EventTagModel) {
        // Remove tag from junction table
        // Remove tag from tag table
        tag.map(EventTagModel::tagID).toTypedArray()
           .let(this::junctionFromID).toTypedArray()
           .let(this::delete)
        _delete(*tag)
    }
    @Delete protected abstract fun _delete(vararg tag: EventTagModel)

    @Query("Select * From EventTag")
    abstract override fun getAllTags(): Flow<List<EventTagModel>>

    @Query("Select * From EventTag Where tagID like :id")
    abstract override fun fromID(id: UUID): Flow<EventTagModel?>
    @Query("Select * From EventTag Where content like :content")
    abstract override fun fromName(content: String): Flow<List<EventTagModel>>

    @Query("Select * From Event Where eventID == :eventID")
    @Transaction abstract override fun forEvent(eventID: UUID): Flow<EventWithTags?>
    @Query("Select * From EventTag Where tagID == :tagID")
    @Transaction abstract override fun withTag(tagID: UUID): Flow<EventsWithTag>

    @Insert protected abstract fun insert(vararg junction: EventTagJunction)
    @Delete protected abstract fun delete(vararg junction: EventTagJunction)
    @Query("Select * From EventTagJunction Where tagID == :id")
    protected abstract fun junctionFromID(vararg id: UUID): List<EventTagJunction>

    override fun associate(event: EventModel, tag: EventTagModel) {
        insert(EventTagJunction(tag.tagID, event.eventID))
    }
    @Transaction override fun associate(event: EventModel, tags: List<EventTagModel>) {
        tags.map { EventTagJunction(it.tagID, event.eventID) }
            .toTypedArray()
            .let(this::insert)
    }
}
