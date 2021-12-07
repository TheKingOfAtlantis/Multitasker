package uk.co.sksulai.multitasker.db.model

import java.time.*
import android.net.Uri
import androidx.room.*

@Entity data class UserModel(
    @PrimaryKey val ID: String, // ID of the user - Same as Firebase uid

    // Metadata
    val Creation: Instant,      // User creation timestamp
    val LastModified: Instant,  // When user last modified

    val DisplayName: String?,   // User display name
    val Email: String?,         // Email associated with the account

    // Additional user info
    val PreferredHome: String,  // User's preferred "home" screen
    val Avatar: Uri?,           // Avatar icon
    val ActualName: String?,    // User's IRL name
    val Home: String?,          // Address of the user's home
    val DOB: LocalDate?         // Date of Birth
)
