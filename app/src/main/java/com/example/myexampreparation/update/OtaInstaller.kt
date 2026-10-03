package com.example.myexampreparation.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider

sealed class InstallResult {
    object Success : InstallResult()
    data class PermissionRequired(val intent: Intent) : InstallResult()
    data class Error(val message: String) : InstallResult()
}

object OtaInstaller {

    private const val TAG = "OTA_UPDATE"

    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun createManageUnknownAppSourcesIntent(context: Context): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            null
        }
    }

    fun installApk(context: Context, versionCode: Int, downloadId: Long): InstallResult {
        Log.i(TAG, "User triggered APK installation for versionCode: $versionCode (Download ID: $downloadId)")

        // 1. Verify DownloadManager status
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            ?: return InstallResult.Error("DownloadManager service not available")

        val status = OtaDownloader.getDownloadStatus(downloadManager, downloadId)
        if (status != DownloadManager.STATUS_SUCCESSFUL) {
            val errorMsg = "DownloadManager reports status $status (expected STATUS_SUCCESSFUL = ${DownloadManager.STATUS_SUCCESSFUL})"
            Log.e(TAG, errorMsg)
            return InstallResult.Error(errorMsg)
        }

        // 2. Verify APK file exists
        val apkFile = OtaDownloader.getApkFile(context, versionCode)
        if (!apkFile.exists() || apkFile.length() <= 0) {
            val errorMsg = "Downloaded APK file does not exist or is empty at path: ${apkFile.absolutePath}"
            Log.e(TAG, errorMsg)
            return InstallResult.Error(errorMsg)
        }

        // 3. Check Unknown App Install Permission (Android 8.0+)
        if (!canRequestPackageInstalls(context)) {
            Log.w(TAG, "REQUEST_INSTALL_PACKAGES permission not allowed. Prompting user to enable in Settings.")
            val manageIntent = createManageUnknownAppSourcesIntent(context)
            if (manageIntent != null) {
                return InstallResult.PermissionRequired(manageIntent)
            }
        }

        // 4. Generate secure FileProvider content URI
        val contentUri: Uri = try {
            val authority = "${context.packageName}.fileprovider"
            FileProvider.getUriForFile(context, authority, apkFile)
        } catch (e: Exception) {
            val errorMsg = "Failed to generate FileProvider content URI: ${e.localizedMessage}"
            Log.e(TAG, errorMsg, e)
            return InstallResult.Error(errorMsg)
        }

        Log.i(TAG, "Generated secure content URI for package installer: $contentUri")

        // 5. Build and launch system Package Installer Intent
        return try {
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(installIntent)
            Log.i(TAG, "Successfully launched Android system Package Installer for: ${apkFile.name}")
            InstallResult.Success
        } catch (e: Exception) {
            val errorMsg = "Failed to launch Android Package Installer: ${e.localizedMessage}"
            Log.e(TAG, errorMsg, e)
            InstallResult.Error(errorMsg)
        }
    }
}
