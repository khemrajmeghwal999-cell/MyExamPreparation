package com.example.myexampreparation.update

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

sealed class UpdateCheckResult {
    data class UpdateAvailable(
        val latestVersionCode: Int,
        val latestVersionName: String = "",
        val apkUrl: String,
        val releaseNotes: String,
        val forceUpdate: Boolean = false
    ) : UpdateCheckResult()

    object UpToDate : UpdateCheckResult()

    data class Error(val message: String) : UpdateCheckResult()
}

object AppUpdater {

    private const val TAG = "OTA_UPDATE"

    const val GITHUB_RELEASE_API_URL = "https://api.github.com/repos/khemrajmeghwal999-cell/MyExamPreparation/releases/latest"
    const val DEFAULT_VERSION_JSON_URL = "https://raw.githubusercontent.com/khemrajmeghwal999-cell/MyExamPreparation/main/version.json"

    fun getCurrentVersionCode(context: Context): Int {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (e: Exception) {
            1
        }
    }

    fun getCurrentVersionName(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.1"
        } catch (e: Exception) {
            "1.1"
        }
    }

    suspend fun checkForUpdate(
        context: Context,
        githubApiUrl: String = GITHUB_RELEASE_API_URL,
        versionJsonUrl: String = DEFAULT_VERSION_JSON_URL
    ): UpdateCheckResult = withContext(Dispatchers.IO) {
        val currentVersionCode = getCurrentVersionCode(context)
        val currentVersionName = getCurrentVersionName(context)

        // Attempt 1: Check GitHub Releases API
        try {
            val url = URL(githubApiUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", "MyExamPreparation-Android")
            connection.connectTimeout = 6000
            connection.readTimeout = 6000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val jsonBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    jsonBuilder.append(line)
                }
                reader.close()
                connection.disconnect()

                val jsonObject = JSONObject(jsonBuilder.toString())
                val tagName = jsonObject.optString("tag_name", "").trim()
                val releaseNotes = jsonObject.optString("body", "")

                var apkUrl = ""
                val assets = jsonObject.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkUrl = asset.optString("browser_download_url", "")
                            break
                        }
                    }
                }

                if (apkUrl.isBlank() && tagName.isNotBlank()) {
                    apkUrl = "https://github.com/khemrajmeghwal999-cell/MyExamPreparation/releases/download/$tagName/app-release.apk"
                }

                val cleanTag = tagName.removePrefix("v").removePrefix("V").trim()
                val cleanCurrent = currentVersionName.removePrefix("v").removePrefix("V").trim()

                if (cleanTag.isNotBlank() && isVersionNewer(cleanTag, cleanCurrent)) {
                    return@withContext UpdateCheckResult.UpdateAvailable(
                        latestVersionCode = currentVersionCode + 1,
                        latestVersionName = tagName,
                        apkUrl = apkUrl,
                        releaseNotes = releaseNotes,
                        forceUpdate = false
                    )
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "GitHub Releases API check skipped: ${e.message}")
        }

        // Attempt 2: Fallback version.json URL
        try {
            val url = URL(DEFAULT_VERSION_JSON_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 6000
            connection.readTimeout = 6000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val jsonBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    jsonBuilder.append(line)
                }
                reader.close()
                connection.disconnect()

                val jsonObject = JSONObject(jsonBuilder.toString())
                val latestVersionCode = jsonObject.optInt("latestVersionCode", -1)
                val latestVersionName = jsonObject.optString("latestVersionName", "v1.2.0")
                val apkUrl = jsonObject.optString("apkUrl", "")
                val releaseNotes = jsonObject.optString("releaseNotes", "")
                val forceUpdate = jsonObject.optBoolean("forceUpdate", false)

                if (latestVersionCode > currentVersionCode && apkUrl.isNotBlank()) {
                    return@withContext UpdateCheckResult.UpdateAvailable(
                        latestVersionCode = latestVersionCode,
                        latestVersionName = latestVersionName,
                        apkUrl = apkUrl,
                        releaseNotes = releaseNotes,
                        forceUpdate = forceUpdate
                    )
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "version.json check skipped: ${e.message}")
        }

        return@withContext UpdateCheckResult.UpToDate
    }

    private fun isVersionNewer(remoteVersion: String, currentVersion: String): Boolean {
        return try {
            val remoteParts = remoteVersion.split(".").mapNotNull { it.toIntOrNull() }
            val currentParts = currentVersion.split(".").mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(remoteParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val r = remoteParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
            false
        } catch (e: Exception) {
            false
        }
    }
}
