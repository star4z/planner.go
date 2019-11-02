package go.planner.plannergo

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.gson.Gson
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class DriveStorage(drive: Drive) {
    private val mExecutor: Executor = Executors.newSingleThreadExecutor() as Executor
    private val mDriveService: Drive? = drive

    fun createBackup(fileName: String, assignments: List<Assignment>): Task<String> {
        return Tasks.call(mExecutor, Callable {
            val type = "application/json"
            val metadata = File().apply {
                parents = listOf("root")
                mimeType = type
                name = fileName
            }

            val gson = Gson()

            val content = gson.toJson(assignments)

            // Convert content to an AbstractInputStreamContent instance.
            val contentStream: ByteArrayContent? = ByteArrayContent.fromString(type, content)

            // Update the metadata and contents.
            val googleFile = mDriveService!!.files().create(metadata, contentStream).execute()
                    ?: throw IOException("Null result when requesting file creation.")

            googleFile.id
        })
    }
}