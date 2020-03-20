package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.KEY_IMAGE_URI
import timber.log.Timber

class BlurWorker(private val context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {

        val resourceUri = inputData.getString(KEY_IMAGE_URI)

        makeStatusNotification("Blurring image", applicationContext)

        sleep()

        return try {

            if (TextUtils.isEmpty(resourceUri)) {
                Timber.e("Invalid input uri")
                throw  IllegalArgumentException("Invalid input uri")
            }
            val resolver = applicationContext.contentResolver

            val picture = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)))

            val output = blurBitmap(picture, applicationContext)

            //Write bitmap to a temp file
            val outputUri = writeBitmapToFile(applicationContext, output)

            val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())

            Result.success(outputData)

        } catch (e: Throwable) {
            Timber.e("Error applying blur")
            Result.failure()
        }


    }
}