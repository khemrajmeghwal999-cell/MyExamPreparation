package com.example.myexampreparation.update

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

sealed class DownloadState {
    object Idle : DownloadState()
    object Queued : DownloadState()
    data class Downloading(val bytesDownloaded: Long, val totalBytes: Long, val progressPercent: Int) : DownloadState()
    data class Success(val downloadId: Long, val apkFile: File) : DownloadState()
    data class Failed(val reason: String) : DownloadState()
}

object OtaDownloader {

    private const val TAG = "OTA_UPDATE"
    private const val PREF_NAME = "ota_download_prefs"
    private const val KEY_DOWNLOAD_ID = "download_id"
    private const val KEY_VERSION_CODE = "version_code"

    fun getApkFileName(versionCode: Int): String {
        return "MyExamPreparation_Update_$versionCode.apk"
    }

    fun getApkFile(context: Context, versionCode: Int): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: File(context.filesDir, "downloads").apply { mkdirs() }
        return File(dir, getApkFileName(versionCode))
    }

    fun getSavedDownloadId(context: Context, versionCode: Int): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedVersion = prefs.getInt(KEY_VERSION_CODE, -1)
        if (savedVersion == versionCode) {
            return prefs.getLong(KEY_DOWNLOAD_ID, -1L)
        }
        return -1L
    }

    private fun saveDownloadId(context: Context, versionCode: Int, downloadId: Long) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_VERSION_CODE, versionCode)
            .putLong(KEY_DOWNLOAD_ID, downloadId)
            .apply()
    }

    fun startOrResumeDownload(
        context: Context,
        apkUrl: String,
        versionCode: Int
    ): Long {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            ?: run {
                Log.e(TAG, "DownloadManager service not available")
                return -1L
            }

        val existingId = getSavedDownloadId(context, versionCode)
        if (existingId != -1L) {
            val status = getDownloadStatus(downloadManager, existingId)
            if (status == DownloadManager.STATUS_RUNNING || status == DownloadManager.STATUS_PENDING || status == DownloadManager.STATUS_SUCCESSFUL) {
                Log.i(TAG, "Reusing existing download ID: $existingId for versionCode $versionCode (Status: $status)")
                return existingId
            }
        }

        val apkFile = getApkFile(context, versionCode)
        if (apkFile.exists()) {
            apkFile.delete()
        }

        try {
            val request = DownloadManager.Request(Uri.parse(apkUrl)).apply {
                setTitle("My Exam Preparation Update v$versionCode")
                setDescription("Downloading APK update...")
                setMimeType("application/vnd.android.package-archive")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationUri(Uri.fromFile(apkFile))
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            val newDownloadId = downloadManager.enqueue(request)
            saveDownloadId(context, versionCode, newDownloadId)
            Log.i(TAG, "Started new APK download with ID: $newDownloadId for versionCode: $versionCode")
            return newDownloadId
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start DownloadManager task: ${e.localizedMessage}", e)
            return -1L
        }
    }

    fun getDownloadStatus(downloadManager: DownloadManager, downloadId: Long): Int {
        val query = DownloadManager.Query().setFilterById(downloadId)
        downloadManager.query(query)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (statusIdx != -1) {
                    return cursor.getInt(statusIdx)
                }
            }
        }
        return -1
    }

    fun trackDownloadProgress(
        context: Context,
        downloadId: Long,
        versionCode: Int
    ): Flow<DownloadState> = flow {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            ?: run {
                emit(DownloadState.Failed("DownloadManager unavailable"))
                return@flow
            }

        var isDownloading = true
        while (isDownloading) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            var currentStatus = -1
            var bytesDownloaded = 0L
            var totalBytes = 0L
            var reasonCode = -1

            downloadManager.query(query)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val downloadedIdx = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val totalIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val reasonIdx = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)

                    if (statusIdx != -1) currentStatus = cursor.getInt(statusIdx)
                    if (downloadedIdx != -1) bytesDownloaded = cursor.getLong(downloadedIdx)
                    if (totalIdx != -1) totalBytes = cursor.getLong(totalIdx)
                    if (reasonIdx != -1) reasonCode = cursor.getInt(reasonIdx)
                }
            }

            when (currentStatus) {
                DownloadManager.STATUS_PENDING -> {
                    emit(DownloadState.Queued)
                }
                DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PAUSED -> {
                    val progressPercent = if (totalBytes > 0) ((bytesDownloaded * 100) / totalBytes).toInt() else 0
                    emit(DownloadState.Downloading(bytesDownloaded, totalBytes, progressPercent))
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    val apkFile = getApkFile(context, versionCode)
                    Log.i(TAG, "APK download completed successfully! Path: ${apkFile.absolutePath}, Size: ${apkFile.length()} bytes")
                    emit(DownloadState.Success(downloadId, apkFile))
                    isDownloading = false
                }
                DownloadManager.STATUS_FAILED -> {
                    val failureReason = getFailureReasonText(reasonCode)
                    Log.e(TAG, "APK download failed with reason code $reasonCode: $failureReason")
                    emit(DownloadState.Failed(failureReason))
                    isDownloading = false
                }
                else -> {
                    Log.e(TAG, "Unknown or invalid download ID $downloadId")
                    emit(DownloadState.Failed("Download not found or canceled"))
                    isDownloading = false
                }
            }

            if (isDownloading) {
                delay(800)
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun getFailureReasonText(reason: Int): String {
        return when (reason) {
            DownloadManager.ERROR_CANNOT_RESUME -> "Cannot resume download"
            DownloadManager.ERROR_DEVICE_NOT_FOUND -> "External storage not found"
            DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "File already exists"
            DownloadManager.ERROR_FILE_ERROR -> "Storage file error"
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Insufficient storage space"
            DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Too many HTTP redirects"
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Unhandled HTTP response code"
            DownloadManager.ERROR_UNKNOWN -> "Unknown download error"
            else -> "Download failed (Code: $reason)"
        }
    }
}
