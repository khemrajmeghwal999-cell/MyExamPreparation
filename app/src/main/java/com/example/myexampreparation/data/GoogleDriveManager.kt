package com.example.myexampreparation.data

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GoogleDriveManager {

    private const val TAG = "GoogleDriveManager"

    val DRIVE_APPDATA_SCOPE = Scope(DriveScopes.DRIVE_APPDATA)
    val DRIVE_FILE_SCOPE = Scope(DriveScopes.DRIVE_FILE)

    private const val BACKUP_FILE_NAME = "my_exam_prep_backup.json"
    private const val MASTER_QUESTION_BANK_FILE_NAME = "My_Exam_Preparation_Master_Questions.csv"

    private const val PREF_BACKUP_NAME = "google_drive_backup_prefs"
    private const val KEY_LAST_BACKUP_TIMESTAMP = "last_backup_timestamp"
    private const val KEY_MASTER_FILE_ID = "master_question_bank_file_id"

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(DRIVE_APPDATA_SCOPE, DRIVE_FILE_SCOPE)
            .build()

        return GoogleSignIn.getClient(context, gso)
    }

    fun getSignedInAccount(context: Context): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    fun isSignedIn(context: Context): Boolean {
        val account = getSignedInAccount(context)
        return account != null && (
            GoogleSignIn.hasPermissions(account, DRIVE_APPDATA_SCOPE) ||
            GoogleSignIn.hasPermissions(account, DRIVE_FILE_SCOPE)
        )
    }

    fun signOut(context: Context, onComplete: () -> Unit) {
        val client = getGoogleSignInClient(context)
        client.signOut().addOnCompleteListener {
            onComplete()
        }
    }

    private fun getDriveService(context: Context, account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("MyExamPreparation")
            .build()
    }

    private fun formatExceptionMessage(e: Exception): String {
        Log.e(TAG, "Drive operation error", e)

        if (e is GoogleJsonResponseException) {
            val code = e.statusCode
            val details = e.details?.message ?: e.statusMessage ?: e.message
            return "Google Drive API Error ($code): $details"
        }

        val msg = e.localizedMessage ?: e.message
        if (!msg.isNullOrBlank()) {
            return msg
        }

        val causeMsg = e.cause?.localizedMessage ?: e.cause?.message
        if (!causeMsg.isNullOrBlank()) {
            return "${e.javaClass.simpleName}: $causeMsg"
        }

        return "${e.javaClass.simpleName} occurred (${e.hashCode()})"
    }

    fun saveMasterFileId(context: Context, fileId: String) {
        context.getSharedPreferences(PREF_BACKUP_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MASTER_FILE_ID, fileId)
            .apply()
    }

    fun getMasterFileId(context: Context): String? {
        return context.getSharedPreferences(PREF_BACKUP_NAME, Context.MODE_PRIVATE)
            .getString(KEY_MASTER_FILE_ID, null)
    }

    /**
     * Finds the safest / latest Master Question Bank file on Google Drive.
     * Uses stored fileId if valid; otherwise queries space 'drive' with modifiedTime desc.
     */
    private fun findLatestMasterFile(driveService: Drive, context: Context): File? {
        val storedId = getMasterFileId(context)
        if (!storedId.isNullOrBlank()) {
            try {
                val file = driveService.files().get(storedId)
                    .setFields("id, name, modifiedTime, trashed")
                    .execute()

                if (file != null && file.trashed != true && file.name == MASTER_QUESTION_BANK_FILE_NAME) {
                    Log.i(TAG, "Found Master File using stored fileId: $storedId")
                    return file
                }
            } catch (e: Exception) {
                Log.w(TAG, "Stored fileId '$storedId' invalid or trashed, falling back to search", e)
            }
        }

        // Search space "drive" ordered by modifiedTime desc
        val fileList = driveService.files().list()
            .setSpaces("drive")
            .setQ("name = '$MASTER_QUESTION_BANK_FILE_NAME' and trashed = false")
            .setOrderBy("modifiedTime desc")
            .setFields("files(id, name, modifiedTime)")
            .execute()

        val latestFile = fileList.files?.firstOrNull()
        if (latestFile != null) {
            saveMasterFileId(context, latestFile.id)
            Log.i(TAG, "Found Master File via search (latest modifiedTime): ${latestFile.id} (Modified: ${latestFile.modifiedTime})")
            return latestFile
        }

        return null
    }

    /**
     * Uploads the personal backup JSON to Google Drive's hidden appDataFolder.
     * Independent from Master Question Bank.
     */
    suspend fun uploadBackupToDrive(
        context: Context,
        backupJsonString: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        val isHindi = AppLanguageStorage.getSelectedLanguage(context) == AppLanguage.HINDI
        try {
            val account = getSignedInAccount(context)
                ?: return@withContext Result.failure(Exception(if (isHindi) "कृपया पहले गूगल खाते से साइन इन करें।" else "Please sign in with Google first."))

            val driveService = getDriveService(context, account)

            // Search in appDataFolder
            val fileList = driveService.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = '$BACKUP_FILE_NAME' and trashed = false")
                .setFields("files(id, name, modifiedTime)")
                .execute()

            val mediaContent = ByteArrayContent.fromString(
                "application/json",
                backupJsonString
            )

            val timestamp = System.currentTimeMillis()

            if (fileList.files.isNullOrEmpty()) {
                val fileMetadata = File()
                    .setName(BACKUP_FILE_NAME)
                    .setParents(listOf("appDataFolder"))

                driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
            } else {
                val existingFileId = fileList.files[0].id
                val fileMetadata = File()
                    .setName(BACKUP_FILE_NAME)

                driveService.files().update(existingFileId, fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
            }

            saveLastBackupTimestamp(context, timestamp)
            Result.success(timestamp)
        } catch (e: Exception) {
            val errorText = formatExceptionMessage(e)
            Result.failure(Exception(if (isHindi) "बैकअप अपलोड विफल रहा: $errorText" else "Backup Upload Error: $errorText"))
        }
    }

    /**
     * Downloads personal backup JSON from Google Drive's hidden appDataFolder.
     */
    suspend fun downloadBackupFromDrive(
        context: Context
    ): Result<String> = withContext(Dispatchers.IO) {
        val isHindi = AppLanguageStorage.getSelectedLanguage(context) == AppLanguage.HINDI
        try {
            val account = getSignedInAccount(context)
                ?: return@withContext Result.failure(Exception(if (isHindi) "कृपया पहले गूगल खाते से साइन इन करें।" else "Please sign in with Google first."))

            val driveService = getDriveService(context, account)

            val fileList = driveService.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = '$BACKUP_FILE_NAME' and trashed = false")
                .setFields("files(id, name, modifiedTime)")
                .execute()

            if (fileList.files.isNullOrEmpty()) {
                return@withContext Result.failure(Exception(if (isHindi) "गूगल ड्राइव पर कोई बैकअप नहीं मिला।" else "No backup found on Google Drive."))
            }

            val fileId = fileList.files[0].id
            val outputStream = java.io.ByteArrayOutputStream()
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)

            val backupJsonString = outputStream.toString("UTF-8")
            Result.success(backupJsonString)
        } catch (e: Exception) {
            val errorText = formatExceptionMessage(e)
            Result.failure(Exception(if (isHindi) "बैकअप डाउनलोड विफल रहा: $errorText" else "Download Error: $errorText"))
        }
    }

    fun saveLastBackupTimestamp(context: Context, timestamp: Long) {
        context.getSharedPreferences(PREF_BACKUP_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_BACKUP_TIMESTAMP, timestamp)
            .apply()
    }

    fun getLastBackupTimestamp(context: Context): Long {
        return context.getSharedPreferences(PREF_BACKUP_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_BACKUP_TIMESTAMP, 0L)
    }

    // =========================================================================
    // MASTER QUESTION BANK (Normal Visible Google Drive Storage - "My Drive")
    // =========================================================================

    /**
     * Downloads Master Question Bank CSV from normal visible Google Drive ("My Drive").
     */
    suspend fun downloadMasterQuestionBankFromDrive(
        context: Context
    ): Result<String> = withContext(Dispatchers.IO) {
        val isHindi = AppLanguageStorage.getSelectedLanguage(context) == AppLanguage.HINDI
        try {
            val account = getSignedInAccount(context)
                ?: return@withContext Result.failure(Exception(if (isHindi) "कृपया पहले गूगल खाते से साइन इन करें।" else "Please sign in with Google first."))

            val driveService = getDriveService(context, account)

            val masterFile = findLatestMasterFile(driveService, context)
                ?: return@withContext Result.failure(Exception(if (isHindi) "गूगल ड्राइव पर मास्टर प्रश्न बैंक नहीं मिला।" else "Master Question Bank not found on Google Drive."))

            val fileId = masterFile.id
            Log.i(TAG, "Downloading Master Question Bank CSV using File ID: $fileId (Modified: ${masterFile.modifiedTime})")

            val outputStream = java.io.ByteArrayOutputStream()
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)

            val csvString = outputStream.toString("UTF-8")
            val rowCount = csvString.lines().count { it.trim().isNotEmpty() } - 1
            Log.i(TAG, "Successfully downloaded Master File ID: $fileId | Questions Row Count: $rowCount")

            Result.success(csvString)
        } catch (e: Exception) {
            val errorText = formatExceptionMessage(e)
            Result.failure(Exception(if (isHindi) "सिंक विफल रहा: $errorText" else "Sync failed: $errorText"))
        }
    }

    /**
     * Uploads local questions as Master Question Bank CSV to normal visible Google Drive ("My Drive").
     */
    suspend fun uploadMasterQuestionBankToDrive(
        context: Context,
        csvContent: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        val isHindi = AppLanguageStorage.getSelectedLanguage(context) == AppLanguage.HINDI
        try {
            val account = getSignedInAccount(context)
                ?: return@withContext Result.failure(Exception(if (isHindi) "कृपया पहले गूगल खाते से साइन इन करें।" else "Please sign in with Google first."))

            val driveService = getDriveService(context, account)

            val masterFile = findLatestMasterFile(driveService, context)

            val mediaContent = ByteArrayContent.fromString(
                "text/csv",
                csvContent
            )

            val timestamp = System.currentTimeMillis()

            if (masterFile == null) {
                // Create new file in My Drive
                val fileMetadata = File()
                    .setName(MASTER_QUESTION_BANK_FILE_NAME)

                val createdFile = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()

                saveMasterFileId(context, createdFile.id)
                Log.i(TAG, "Created new Master File on Google Drive. File ID: ${createdFile.id}")
            } else {
                // Update existing file in My Drive (prevent duplicates)
                val existingFileId = masterFile.id
                val fileMetadata = File()
                    .setName(MASTER_QUESTION_BANK_FILE_NAME)

                driveService.files().update(existingFileId, fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()

                saveMasterFileId(context, existingFileId)
                Log.i(TAG, "Updated existing Master File on Google Drive. File ID: $existingFileId")
            }

            Result.success(timestamp)
        } catch (e: Exception) {
            val errorText = formatExceptionMessage(e)
            Result.failure(Exception(if (isHindi) "मास्टर प्रश्न बैंक अपलोड विफल रहा: $errorText" else "Master Question Bank upload failed: $errorText"))
        }
    }

    /**
     * Checks if Master Question Bank file exists on Google Drive's My Drive.
     */
    suspend fun checkMasterFileExistsOnDrive(
        context: Context
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val account = getSignedInAccount(context) ?: return@withContext false
            val driveService = getDriveService(context, account)

            val masterFile = findLatestMasterFile(driveService, context)
            masterFile != null
        } catch (e: Exception) {
            Log.e(TAG, "checkMasterFileExistsOnDrive error", e)
            false
        }
    }
}
