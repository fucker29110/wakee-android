package com.wakee.app.util

import android.content.Context
import java.io.File
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmAudioCache @Inject constructor() {

    private val cacheDir = "alarm_audio_cache"

    suspend fun getOrDownload(context: Context, url: String): File? = withContext(Dispatchers.IO) {
        try {
            val fileName = url.hashCode().toString() + ".m4a"
            val dir = File(context.cacheDir, cacheDir)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)

            if (file.exists()) return@withContext file

            val bytes = URL(url).readBytes()
            file.writeBytes(bytes)
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearCache(context: Context) {
        val dir = File(context.cacheDir, cacheDir)
        if (dir.exists()) dir.deleteRecursively()
    }
}
