package uk.co.sksulai.multitasker.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import uk.co.sksulai.multitasker.db.repo.UserRepository

class UserViewModel(app: Application) : AndroidViewModel(app) {
    private val userRepo by lazy { UserRepository(app) }

    val currentUser = userRepo.currentUser
}
