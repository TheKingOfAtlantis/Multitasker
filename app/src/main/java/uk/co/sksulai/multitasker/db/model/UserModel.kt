package uk.co.sksulai.multitasker.db.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity data class UserModel(
    @PrimaryKey val ID: String, // ID of the user
    val FirebaseID: String?,    // Firebase uid for the User
    val DisplayName: String?,   // User display name
    val Email: String?,         // Email associated with the account

    // Additional user info
    val Avatar: Uri?,           // Avatar icon
    val ActualName: String?,    // User's IRL name
    val Home: String?,          // Address of the user's home
    val DOB: LocalDate?         // Date of Birth
)
