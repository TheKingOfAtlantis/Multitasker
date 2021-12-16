package uk.co.sksulai.multitasker.db.dao

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import androidx.test.filters.SmallTest
import androidx.test.ext.junit.runners.AndroidJUnit4

import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest

import com.google.common.truth.Truth.assertThat

import android.content.Context

import uk.co.sksulai.multitasker.db.LocalDB
import uk.co.sksulai.multitasker.util.UserTestUtil

import kotlin.random.Random

@HiltAndroidTest @RunWith(AndroidJUnit4::class)
@SmallTest class UserDaoTest {
    @get:Rule var hiltAndroidRule = HiltAndroidRule(this)

    @Inject @ApplicationContext lateinit var context: Context
    @Inject lateinit var db: LocalDB
    @Inject lateinit var dao: UserDao

    @Before fun createDB() = hiltAndroidRule.inject()
    @After fun closeDB()   = db.close()

    @Test fun writeUserAndGetAll(): Unit = runBlocking {
        val user = UserTestUtil.createList(Random.nextInt(10, 100))

        // Assert that the database is empty
        assertThat(dao.getAll().first()).isEmpty()
        // Then assert that all the users we added are present (and nothing else)
        user.onEach { dao.insert(it) }
        assertThat(dao.getAll().first()).containsExactlyElementsIn(user)
    }
    @Test fun writeUsersAndDeleteAll(): Unit = runBlocking {
        val user = UserTestUtil.createList(Random.nextInt(10, 100))

        // Assert that all the users we added are present (and nothing else)
        user.onEach { dao.insert(it) }
        assertThat(dao.getAll().first()).containsExactlyElementsIn(user)
        // Then assert that all the users we added are removed
        dao.deleteAll()
        assertThat(dao.getAll().first()).isEmpty()
    }
    @Test fun writeUsersAndDeleteSingle(): Unit = runBlocking {
        val users    = UserTestUtil.createList(Random.nextInt(10, 100))
        val toDelete = users.random()

        // Assert that all the users we added are present (and nothing else)
        users.onEach { dao.insert(it) }
        assertThat(dao.getAll().first()).containsExactlyElementsIn(users)

        // Remove the user and assert only that user was removed
        dao.delete(toDelete)
        assertThat(dao.getAll().first()).apply {
            doesNotContain(toDelete) // So check that it has been deleted
            containsExactlyElementsIn(users.filter{ it != toDelete }) // Check everything else is still present
        }
    }

    @Test fun writeUserAndRemove(): Unit = runBlocking {
        val user = UserTestUtil.createSingle()

        // Assert that it doesn't already exist (even though we haven't added it)
        assertThat(dao.fromID(user.ID).first()).isNull()
        // Then assert that does exist now that we have added it
        dao.insert(user)
        assertThat(dao.fromID(user.ID).first()).isEqualTo(user)
        // Then assert that doesn't exist now that we have removed it
        dao.delete(user)
        assertThat(dao.fromID(user.ID).first()).isNull()
    }
    @Test fun writeUserAndUpdate(): Unit = runBlocking {
        val user = UserTestUtil.createSingle()

        // Assert that does exist now that we have added it
        dao.insert(user)
        assertThat(dao.fromID(user.ID).first()).isEqualTo(user)
        // Then assert that once updated is not the same as original
        dao.update(user.copy(DisplayName = "Username"))
        dao.fromID(user.ID).first().also { updatedUser ->
            assertThat(updatedUser).apply {
                isNotNull()
                isNotEqualTo(user)
            }

            // Assert no other field has changed
            updatedUser!!.javaClass.declaredFields.filter { it.name != "DisplayName" }.forEach {
                it.isAccessible   = true // For the purpose of testing we need to access the field (it is private)

                val newValue      = it.get(updatedUser)
                val originalValue = it.get(user)

                assertThat(newValue).isEqualTo(originalValue)
            }
        }
    }

    @Test fun writeUserAndReadByActualName(): Unit = runBlocking {
        val users = UserTestUtil.createList(3).map { it.copy(ActualName = "Dave")  }.onEach { dao.insert(it) } +
                    UserTestUtil.createList(2).map { it.copy(ActualName = "Harry") }.onEach { dao.insert(it) }

        assertThat(dao.fromActualName("Dave").first()).apply {
            hasSize(3)
            containsExactlyElementsIn(users.slice(0..2))
            containsNoneIn(users.slice(3..4))
        }
        assertThat(dao.fromActualName("Harry").first()).apply {
            hasSize(2)
            containsExactlyElementsIn(users.slice(3..4))
            containsNoneIn(users.slice(0..2))
        }
        assertThat(dao.fromActualName("Bob").first()).apply {
            isEmpty()
        }
    }
    @Test fun writeUserAndReadByDisplayName(): Unit = runBlocking {
        val users = UserTestUtil.createList(5)
            .mapIndexed { i, user -> user.copy(DisplayName = "Username$i") }
            .onEach { dao.insert(it) }

        (0..4).forEach {
            assertThat(dao.fromDisplayName("Username$it").first()).apply {
                contains(users[it])
                users.filterIndexed { i,_ -> i != it }
                     .forEach { user -> doesNotContain(user) }
            }
        }

        assertThat(dao.fromDisplayName("Username5").first()).apply {
            isEmpty()
        }
    }

    @Test fun searchByDisplayName(): Unit = runBlocking {
        val users = UserTestUtil.createList(5)
            .mapIndexed { i, user -> user.copy(DisplayName = "Username$i") }
            .onEach { dao.insert(it) }

        // Should contain all the users which start with Username (which is all of them)
        assertThat(dao.fromDisplayName("Username").first())
            .containsExactlyElementsIn(users)

        (0..users.lastIndex).forEach { index ->
            assertThat(dao.fromDisplayName("$index").first())
                .containsExactly(users[index])
        }
    }
    @Test fun searchByActualName(): Unit = runBlocking {
        val users = UserTestUtil.createList(5)
            .mapIndexed { i, user -> user.copy(ActualName = "Actual Name$i") }
            .onEach { dao.insert(it) }

        // Should contain all the users which start with 'Actual' (which is all of them)
        assertThat(dao.fromActualName(searchQuery("Actual") { any = true }).first())
            .containsExactlyElementsIn(users)
        // Should contain all the users which start with 'Name' (which is all of them)
        assertThat(dao.fromActualName(searchQuery("Name") { any = true }).first())
            .containsExactlyElementsIn(users)

        (0..users.lastIndex).forEach { index ->
            assertThat(dao.fromActualName("$index").first())
                .containsExactly(users[index])
        }
    }
}
