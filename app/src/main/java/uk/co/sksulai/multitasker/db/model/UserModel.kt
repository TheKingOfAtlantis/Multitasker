package uk.co.sksulai.multitasker.db.model

import java.time.*
import android.net.Uri
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.room.*
import androidx.compose.runtime.Immutable
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import kotlinx.parcelize.Parcelize

import com.google.firebase.auth.FirebaseUser

import uk.co.sksulai.multitasker.R

/**
 *
 * @param userID        ID of the user (Same as [FirebaseUser.getUid])
 * @param creation      User creation timestamp
 * @param lastModified  When user data was last modified
 *
 * @param displayName   User display name
 * @param email         Email associated with the account
 *
 * @param preferredHome User's preferred "home" screen
 * @param avatar        Avatar icon
 * @param actualName    User's IRL name
 * @param home          Address of the user's home
 * @param dob           Date of Birth
 */
@Immutable @Parcelize
@Entity data class UserModel(
    @PrimaryKey val userID: String,

    // Metadata
    val creation: Instant,
    val lastModified: Instant,

    val displayName: String,
    val email: String,

    // Additional user info
    val preferredHome: String,
    val avatar: Uri?,
    val actualName: String?,
    val home: String?,
    val dob: LocalDate?
) : Parcelable

/**
 * Painter which retrieves the user's profile picture [UserModel.avatar]
 */
val UserModel.avatarPainter @Composable get() = rememberImagePainter(avatar) {
    crossfade(true)
    placeholder(R.drawable.ic_baseline_account_circle)
    error(R.drawable.ic_baseline_account_circle)
    transformations(CircleCropTransformation())
}
