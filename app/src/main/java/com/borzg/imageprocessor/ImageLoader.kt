package com.borzg.imageprocessor

import android.content.Context
import android.graphics.BitmapFactory

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class ImageLoader(
    private val appContext: Context
) {

    fun loadFileFromUrl(url: String): File {
        val srcUrl = URL(url)
        val connection: HttpURLConnection = srcUrl
            .openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        val input = connection.inputStream
        return input.use { inputStream ->
            val file = File(appContext.cacheDir, "cacheFileAppeal.srl")
            FileOutputStream(file).use { output ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
            file
        }
    }

    fun loadBitmapFromURL(src: String): Bitmap? {
        val url = URL(src)
        val connection: HttpURLConnection = url
            .openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        val input: InputStream = connection.inputStream
        return BitmapFactory.decodeStream(input)
    }
}