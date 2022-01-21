package uk.co.sksulai.multitasker.db.web

import kotlin.random.Random

import javax.inject.Inject
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest

import org.junit.*
import org.junit.runner.RunWith
import androidx.test.filters.MediumTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

import uk.co.sksulai.multitasker.db.dao.SearchQuery
import uk.co.sksulai.multitasker.util.*

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@MediumTest class UserWebServiceTest {
    @get:Rule(order = 0) var hiltAndroidRule     = HiltAndroidRule(this)
    @get:Rule(order = 1) var instantExecutorRule = InstantTaskExecutorRule()

    @Inject lateinit var web: UserWebService

    @Before fun createDB() = hiltAndroidRule.inject()
    @After fun closeDB()   = runBlocking {
        FirebaseEmulatorUtil.db.deleteDocuments()
        FirebaseEmulatorUtil.auth.deleteAccounts()
    }

    @Test fun writeUserAndGetAll(): Unit = runBlocking {
        val user = UserTestUtil.createList(Random.nextInt(10, 100))

        // Assert that the database is empty
        assertThat(web.getAll().first()).isEmpty()
        // Then assert that all the users we added are present (and nothing else)
        user.onEach { web.insert(it) }
        assertThat(web.getAll().first()).containsExactlyElementsIn(user)
    }
    @Test fun writeUsersAndDeleteSingle(): Unit = runBlocking {
        val users    = UserTestUtil.createList(Random.nextInt(10, 100))
        val toDelete = users.random()

        // Assert that all the users we added are present (and nothing else)
        users.onEach { web.insert(it) }
        assertThat(web.getAll().first()).containsExactlyElementsIn(users)

        // Remove the user and assert only that user was removed
        web.delete(toDelete)
        assertThat(web.getAll().first()).apply {
            doesNotContain(toDelete) // So check that it has been deleted
            containsExactlyElementsIn(users.filter{ it != toDelete }) // Check everything else is still present
        }
    }

    @Test fun writeUserAndRemove(): Unit = runBlocking {
        val user = UserTestUtil.createSingle()

        // Assert that it doesn't already exist (even though we haven't added it)
        assertThat(web.fromID(user.userID).first()).isNull()
        // Then assert that does exist now that we have added it
        web.insert(user)
        assertThat(web.fromID(user.userID).first()).isEqualTo(user)
        // Then assert that doesn't exist now that we have removed it
        web.delete(user)
        assertThat(web.fromID(user.userID).first()).isNull()
    }
    @Test fun writeUserAndUpdate(): Unit = runBlocking {
        val user = UserTestUtil.createSingle(useAuth = true)

        // Assert that does exist now that we have added it
        web.insert(user)
        assertThat(web.fromID(user.userID).first()).isEqualTo(user)
        // Then assert that once updated is not the same as original
        web.update(user.copy(displayName = "Username"))
        web.fromID(user.userID).first().also { updatedUser ->
            assertThat(updatedUser).apply {
                isNotNull() // Ensure we definitely have a value
                isNotEqualTo(user) // But that value is not equal to what we had before
            }

            // Assert no other field has changed
            updatedUser!!.javaClass.declaredFields.filter { it.name != "displayName" }.forEach {
                it.isAccessible   = true // For the purpose of testing we need to access the field (it is private)

                val newValue      = it.get(updatedUser)
                val originalValue = it.get(user)

                assertThat(newValue).isEqualTo(originalValue)
            }
        }
    }

    @Test fun writeUserAndReadByActualName(): Unit = runBlocking {
        val users = UserTestUtil.createList(3).map { it.copy(actualName = "Dave")  }.onEach { web.insert(it) } +
                    UserTestUtil.createList(2).map { it.copy(actualName = "Harry") }.onEach { web.insert(it) }

        assertThat(web.fromActualName("Dave").first()).apply {
            hasSize(3)
            containsExactlyElementsIn(users.slice(0..2))
            containsNoneIn(users.slice(3..4))
        }
        assertThat(web.fromActualName("Harry").first()).apply {
            hasSize(2)
            containsExactlyElementsIn(users.slice(3..4))
            containsNoneIn(users.slice(0..2))
        }
        assertThat(web.fromActualName("Bob").first()).apply {
            isEmpty()
        }
    }
    @Test fun writeUserAndReadByDisplayName(): Unit = runBlocking {
        val users = UserTestUtil.createList(5)
            .mapIndexed { i, user -> user.copy(displayName = "Username$i") }
            .onEach { web.insert(it) }

        (0..4).forEach { assertThat(web.fromDisplayName("Username$it").first()).containsExactly(users[it]) }
        assertThat(web.fromDisplayName("Username5").first()).isEmpty()
    }

    @Test fun searchByDisplayName(): Unit = runBlocking {
        val users = UserTestUtil.createList(5)
            .mapIndexed { i, user -> user.copy(displayName = "Username$i") }
            .onEach { web.insert(it) }

        // Should contain all the users which start with Username (which is all of them)
        assertThat(web.fromDisplayName(SearchQuery.remote("Username") { anyEnd = true }).first())
            .containsExactlyElementsIn(users)
    }
    @Test fun searchByActualName(): Unit = runBlocking {
        val users = UserTestUtil.createList(5)
            .mapIndexed { i, user -> user.copy(actualName = "Actual Name$i") }
            .onEach { web.insert(it) }

        // Should contain all the users which start with 'Actual' (which is all of them)
        assertThat(web.fromActualName(SearchQuery.remote("Actual") { anyEnd = true }).first())
            .containsExactlyElementsIn(users)
    }
}
