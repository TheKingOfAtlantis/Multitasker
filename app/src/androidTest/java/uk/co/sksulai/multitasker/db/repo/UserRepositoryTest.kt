package uk.co.sksulai.multitasker.db.repo

import org.junit.*
import org.junit.runner.RunWith
import com.google.common.truth.Truth.*

import javax.inject.Inject

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.qualifiers.ApplicationContext

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.arch.core.executor.testing.InstantTaskExecutorRule

import android.content.Context
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthException

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

import uk.co.sksulai.multitasker.db.LocalDB
import uk.co.sksulai.multitasker.db.dao.UserDao
import uk.co.sksulai.multitasker.db.web.UserWebService
import uk.co.sksulai.multitasker.db.model.UserModel

import uk.co.sksulai.multitasker.util.RandomUtil
import uk.co.sksulai.multitasker.util.FirebaseEmulatorUtil
import uk.co.sksulai.multitasker.util.DatastoreLocators.AppState

@HiltAndroidTest @RunWith(AndroidJUnit4::class)
class UserRepositoryTest {
    @get:Rule var hiltAndroidRule     = HiltAndroidRule(this)
    @get:Rule var instantExecutorRule = InstantTaskExecutorRule()

    @ApplicationContext @Inject lateinit var context: Context
    @Inject lateinit var repo: UserRepository

    @Inject lateinit var db: LocalDB
    @Inject lateinit var dao: UserDao
    @Inject lateinit var web: UserWebService

    @Before fun setup()  = hiltAndroidRule.inject()
    @After fun cleanup() = runBlocking {
        FirebaseEmulatorUtil.auth.deleteAccounts()
        FirebaseEmulatorUtil.db.deleteDocuments()
        db.close()
    }

    data class AuthParam(
        val email: String,
        val password: String
    ) {
        companion object {
            fun random() = AuthParam(
                email    = RandomUtil.nextEmail(),
                password = RandomUtil.nextString(8, 12)
            )
        }
    }
    private suspend fun UserRepository.create(auth: AuthParam) = create(auth.email, auth.password)
    private suspend fun UserRepository.authenticate(auth: AuthParam) = authenticate(auth.email, auth.password)

    @Test fun createAndRetrieve(): Unit = runBlocking {
        // Create a user then assert that we get the same thing from both the local and web database
        val user = repo.create(AuthParam.random())
        assertThat(dao.fromID(user.ID).first()).isEqualTo(user)
        assertThat(web.fromID(user.ID).first()).isEqualTo(user)
    }

    @Test fun createAndUpdate(): Unit = runBlocking {
        val user = repo.create(AuthParam.random())

        val userFlow = repo.fromID(user.ID)
        assertThat(userFlow.first()).isEqualTo(user)

        repo.update(user.copy(DisplayName = "Username"))
        // Cannot directly compare due to also modifying the 'LastModified' field
        // So we copy LastModified as everything should match
        assertThat(userFlow.first()?.copy(LastModified = user.LastModified)).apply {
            isEqualTo(user.copy(DisplayName = "Username"))
            isNotEqualTo(user)
        }

        // We also want to make sure that the change is not just made to the local or web database
        assertThat(dao.fromID(user.ID).first()?.copy(LastModified = user.LastModified)).apply {
            isEqualTo(user.copy(DisplayName = "Username"))
            isNotEqualTo(user)
        }
        assertThat(web.fromID(user.ID).first()?.copy(LastModified = user.LastModified)).apply {
            isEqualTo(user.copy(DisplayName = "Username"))
            isNotEqualTo(user)
        }
    }
    @Test fun updateIndirectViaDao(): Unit = runBlocking {
        val user = repo.create(AuthParam.random())

        val userFlow = repo.fromID(user.ID)
        assertThat(userFlow.first()).isEqualTo(user)

        // When we update the local database directly
        dao.update(user.copy(DisplayName = "Username"))

        // Our flow from the repository should have been updated
        assertThat(userFlow.first()?.copy(LastModified = user.LastModified)).apply {
            isEqualTo(user.copy(DisplayName = "Username"))
            isNotEqualTo(user)
        }

        // Additionally if we retrieve from the database it should be changed
        assertThat(dao.fromID(user.ID).first()?.copy(LastModified = user.LastModified)).apply {
            isEqualTo(user.copy(DisplayName = "Username"))
            isNotEqualTo(user)
        }
        // However, if we retrieve from the web database it should not have changed
        assertThat(web.fromID(user.ID).first()?.copy(LastModified = user.LastModified)).apply {
            isNotEqualTo(userFlow.first())
            isEqualTo(user)
        }
    }
    @Test fun updateIndirectViaWeb(): Unit = runBlocking {
        val user = repo.create(AuthParam.random())

        val userFlow = repo.fromID(user.ID)
        assertThat(userFlow.first()).isEqualTo(user)

        // When we update the local database directly
        web.update(user.copy(DisplayName = "Username"))

        // Our flow from the repository should have been updated
        assertThat(userFlow.first()?.copy(LastModified = user.LastModified)).apply {
            isEqualTo(user.copy(DisplayName = "Username"))
            isNotEqualTo(user)
        }

        // Additionally if we retrieve from the database it should be changed
        assertThat(web.fromID(user.ID).first()?.copy(LastModified = user.LastModified)).apply {
            isEqualTo(user.copy(DisplayName = "Username"))
            isNotEqualTo(user)
        }
        // The repository should cache changes to users to the local database
        assertThat(dao.fromID(user.ID).first()?.copy(LastModified = user.LastModified)).apply {
            isEqualTo(user.copy(DisplayName = "Username"))
            isNotEqualTo(user)
        }
    }

    @Test fun createAndDeleteEverywhere(): Unit = runBlocking {
        val auth = AuthParam.random()
        val user = repo.create(auth)

        repo.delete(user, localOnly = false)

        val datastore = AppState.retrieve(context)
        assertThat(datastore.data.first()[AppState.CurrentUser]).isNull()

        assertThat(dao.fromID(user.ID).first()).isNull()
        assertThat(web.fromID(user.ID).first()).isNull()
    }
    @Test fun createAndDeleteLocal(): Unit = runBlocking {
        val auth = AuthParam.random()
        val user = repo.create(auth)

        repo.delete(user, localOnly = true)

        val datastore = AppState.retrieve(context)
        assertThat(datastore.data.first()[AppState.CurrentUser]).isNull()

        assertThat(dao.fromID(user.ID).first()).isNull()
        assertThat(web.fromID(user.ID).first()).apply {
            isNotNull()
            isEqualTo(user)
        }
    }
    @Test(expected = FirebaseAuthException::class) fun throwWhenSignInAsNonexistentUser(): Unit = runBlocking {
        // Trying to sign in (since we have no users) should throw an exception
        repo.authenticate(AuthParam.random())
    }

    @Test fun validateCurrentUserState(): Unit = runBlocking {
        var currentUser: UserModel? = null
        var lastUser: UserModel?    = null

        suspend fun runAsserts() {
            val datastore = AppState.retrieve(context)
            datastore.data.first().let {
                val id = it[AppState.CurrentUser]
                if(currentUser != null || lastUser != null) assertThat(id).apply {
                    isEqualTo(currentUser?.ID)
                    isNotEqualTo(lastUser?.ID)
                }
            }
            repo.currentUser.first().let {
                if (currentUser != null || lastUser != null) assertThat(it).apply {
                    isEqualTo(currentUser)
                    isNotEqualTo(lastUser)
                }
            }
        }

        val auth = AuthParam.random()

        // We create our first user
        // Expect that the current user is equal to this new user
        lastUser    = null
        currentUser = repo.create(auth)
        runAsserts()

        // Create another user
        // Expect the current user to switch to that new user
        lastUser    = currentUser
        currentUser = repo.create(AuthParam.random())
        runAsserts()

        // Now we sign in as the first user
        run {
            val tempUser = lastUser
            lastUser     = currentUser
            currentUser  = tempUser
        }

        val userId = repo.authenticate(auth)
        assertThat(userId).isEqualTo(currentUser?.ID)
        runAsserts()

        // Now we sign out completely
        lastUser    = currentUser
        currentUser = null

        repo.signOut()
        runAsserts()
    }
}
