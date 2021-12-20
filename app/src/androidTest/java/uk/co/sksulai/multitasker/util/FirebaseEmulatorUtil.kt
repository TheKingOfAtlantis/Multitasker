package uk.co.sksulai.multitasker.util

import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.DELETE
import retrofit2.http.GET

data class oobCodes(
    val email: String,
    val oobCode: String,
    val oobLink: String,
    val requestType: String
)
data class smsVerificationCodes(
    val phoneNumber: String,
    val sessionCode: String
)

object FirebaseEmulatorUtil {
    private const val projectID = "multitasker-dfd0d"

    const val ip = "10.0.2.2"
    object port {
        const val db   = 8080
        const val auth = 9099
    }

    object baseUrl {
        const val db   = "http://$ip:${port.db}/emulator/v1/projects/$projectID/"
        const val auth = "http://$ip:${port.auth}/emulator/v1/projects/$projectID/"
    }

    interface FirestoreService {
        object Endpoint {
            const val documents = "/emulator/v1/projects/$projectID/databases/(default)/documents"
        }
        @DELETE(Endpoint.documents) suspend fun deleteDocuments()
    }

    interface AuthService {
        object Endpoint {
            const val accounts = "/emulator/v1/projects/$projectID/accounts"
            const val oobCodes = "/emulator/v1/projects/$projectID/oobCodes"
            const val sms = "/emulator/v1/projects/$projectID/verificationCodes"
        }

        @DELETE(Endpoint.accounts) suspend fun deleteAccounts()
        @GET(Endpoint.oobCodes) suspend fun getOutOfBandAuthCodes(): List<oobCodes>
        @GET(Endpoint.sms) suspend fun getSMSVerificationCodes(): List<smsVerificationCodes>
    }

    private inline fun <reified T> create(baseUrl: String): T =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .build()
            .create()

    val auth get() = create<AuthService>(baseUrl.auth)
    val db   get() = create<FirestoreService>(baseUrl.db)
}
