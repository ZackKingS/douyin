package com.example.douyinandroid.core.core_network.network

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.douyinandroid.common.common_utils.LogUtil
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.IOException

class ContentUriRequestBody(
    private val context: Context,
    private val uri: Uri,
    private val mediaType: MediaType?,
    private val onProgress: ((Int) -> Unit)? = null
) : RequestBody() {

    companion object {
        private const val TAG = "ContentUriRequestBody"
    }

    private var contentLength: Long = 0L

    init {
        contentLength = getContentLength()
        LogUtil.d(TAG, "Created request body: uri=$uri, mediaType=$mediaType, contentLength=$contentLength")
    }

    private fun getContentLength(): Long {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                pfd.statSize
            } ?: 0L
        } catch (e: Exception) {
            LogUtil.w(TAG, "Failed to read content length for uri=$uri: ${e.message}")
            0L
        }
    }

    fun getFileName(): String {
        var name = "file"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex) ?: "file"
            }
        }
        LogUtil.d(TAG, "Resolved file name: $name")
        return name
    }

    override fun contentLength(): Long = contentLength

    override fun contentType(): MediaType? = mediaType

    override fun writeTo(sink: BufferedSink) {
        val totalBytes = contentLength()
        var bytesWritten = 0L
        LogUtil.d(TAG, "Start writing request body: uri=$uri, totalBytes=$totalBytes")

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                sink.write(buffer, 0, bytesRead)
                bytesWritten += bytesRead
                onProgress?.let { callback ->
                    if (totalBytes > 0) {
                        val progress = ((bytesWritten * 100) / totalBytes).toInt().coerceIn(0, 100)
                        callback(progress)
                    }
                }
            }
            sink.flush()
            LogUtil.d(TAG, "Finished writing request body: uri=$uri, bytesWritten=$bytesWritten")
        } ?: throw IOException("Could not open input stream for $uri")
    }
}
