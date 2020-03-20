package com.example.background.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.OUTPUT_PATH
import timber.log.Timber
import java.io.File
import java.lang.Exception
import java.sql.Time

class CleanupWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {

        makeStatusNotification("cleaning up files", applicationContext)

        sleep()

        return try {

            val outputDir = File(applicationContext.filesDir, OUTPUT_PATH)
            if (outputDir.exists()) {
                val entries = outputDir.listFiles()

                if (entries != null) {
                    for (entry in entries) {
                        val name = entry.name
                        if (name.isNotEmpty() && name.endsWith(".png")) {
                            val deleted = entry.delete()
                            Timber.i("Deleted $name - $deleted")
                        }
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure()
        }
    }


}