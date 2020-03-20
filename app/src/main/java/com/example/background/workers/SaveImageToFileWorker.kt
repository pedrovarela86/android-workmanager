package com.example.background.workers

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.KEY_IMAGE_URI
import com.example.background.R
import timber.log.Timber
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class SaveImageToFileWorker(context: Context, parms: WorkerParameters) : Worker(context, parms) {

    private var uri: Uri? = null

    private var stream: OutputStream? = null

    private val title = "Blurred image"

    private val dateFormatter = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault())

    private val resolver = applicationContext.contentResolver

    override fun doWork(): Result {
        makeStatusNotification("Saving image", applicationContext)

        sleep()

        return try {
            val resourceUri = inputData.getString(KEY_IMAGE_URI)
            val bitmap = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)))

            val contentValues = ContentValues().apply {

                put(MediaStore.MediaColumns.DISPLAY_NAME, title)

                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.pathSeparator + applicationContext.resources.getString(R.string.app_name))
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            else
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            uri = resolver.insert(contentUri, contentValues)

            stream = uri?.let { resolver.openOutputStream(it) }

            if (stream != null && bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            }

            resolver.update(contentUri, contentValues, null, null)

            val output = workDataOf(KEY_IMAGE_URI to uri.toString())

            contentValues.clear()

            Result.success(output)

        } catch (e: Exception) {
            uri?.let { resolver.delete(it, null, null) }
            Timber.e(e)
            Result.failure()
        } finally {
            stream?.close()
        }
    }

}