package uk.co.sksulai.multitasker.db.model

import java.time.*
import android.net.Uri
import androidx.room.*
import androidx.compose.runtime.Immutable

/**
 *
 * @param ID            ID of the user - Same as Firebase uid
 * @param Creation      User creation timestamp
 * @param LastModified  When user data was last modified
 *
 * @param DisplayName   User display name
 * @param Email         Email associated with the account
 *
 * @param PreferredHome User's preferred "home" screen
 * @param Avatar        Avatar icon
 * @param ActualName    User's IRL name
 * @param Home          Address of the user's home
 * @param DOB           Date of Birth
 */
@Immutable
@Entity data class UserModel(
    @PrimaryKey val ID: String,

    // Metadata
    val Creation: Instant,
    val LastModified: Instant,

    val DisplayName: String?,
    val Email: String?,

    // Additional user info
    val PreferredHome: String,
    val Avatar: Uri?,
    val ActualName: String?,
    val Home: String?,
    val DOB: LocalDate?
)
