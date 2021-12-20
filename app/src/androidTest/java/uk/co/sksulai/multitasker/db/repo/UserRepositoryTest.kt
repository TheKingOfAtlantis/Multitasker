package uk.co.sksulai.multitasker.db.repo

import org.junit.*
import org.junit.runner.RunWith
import com.google.common.truth.Truth.*

import javax.inject.Inject

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.qualifiers.ApplicationContext

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.arch.core.executor.testing.InstantTaskExecutorRule

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
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

    @Before fun setup()  = hiltAndroidRule.inject()
    @After fun cleanup() = runBlocking {
        FirebaseEmulatorUtil.auth.deleteAccounts()
        FirebaseEmulatorUtil.db.deleteDocuments()
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
