package go.planner.plannergo

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class DriveStorage(drive: Drive) {
    private val TAG = this.javaClass.simpleName

    private val mExecutor: Executor = Executors.newSingleThreadExecutor() as Executor
    private val mDriveService: Drive? = drive

    private val MAX_DRIVE_BACKUPS = 5 //only used if singleDriveMode is enabled

    /**
     * Returns a [FileList] containing all the visible files in the user's My Drive.
     *
     *
     * The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the [Google
 * Developer's Console](https://play.google.com/apps/publish) and be submitted to Google for verification.
     */
    fun queryFiles(): Task<FileList> {
        return Tasks.call(mExecutor, Callable { mDriveService!!.files().list().setSpaces("drive").execute() })
    }

    fun createBackup(fileName: String, assignments: List<Assignment>, singleDriveMode: Boolean = false):
            Task<String> {
        return Tasks.call(mExecutor, Callable {
            val type = "application/json"
            val metadata = File().apply {
                parents = listOf("root")
                mimeType = type
                name = fileName
                val dateTime = DateTime(System.currentTimeMillis())
                Log.d(TAG, "dateTime=$dateTime")
                createdTime = dateTime
                modifiedTime = dateTime
            }

            val gson = Gson()

            val content = gson.toJson(assignments)

            // Convert content to an AbstractInputStreamContent instance.
            val contentStream: ByteArrayContent? = ByteArrayContent.fromString(type, content)

            // Update the metadata and contents.
            val googleFile = mDriveService!!.files().create(metadata, contentStream).execute()
                    ?: throw IOException("Null result when requesting file creation.")

            if (singleDriveMode) {
                queryFiles()
                        .addOnSuccessListener {fileList ->
                            val files = fileList.files
                            Log.d(TAG, files.toString())
                            for (file in files) {
                                Log.d(TAG, "${file.name},c=${file.createdTime},m=${file
                                        .modifiedTime}")
                            }
                            if (files.size > 5) {
                                for (i in 5 until files.size) {
                                    deleteFile(files[i].id)
                                }
                            }
                        }
                        .addOnFailureListener{it.printStackTrace()}
            }

            googleFile.id
        })
    }

    /**
     * Opens the file identified by `fileId` and returns the contents
     */
    fun readFile(fileId: String?): Task<ArrayList<Assignment>>? {
        return Tasks.call(mExecutor, Callable<ArrayList<Assignment>> {
            // Stream the file contents to a String.
            mDriveService!!.files().get(fileId).executeMediaAsInputStream().use { inputStream ->
                //                val stringBuilder = StringBuilder()
//                var line: String
                val scanner = Scanner(inputStream)
                val jsonValue = scanner.nextLine()
                scanner.close()

                val gson = Gson()
                val type = object : TypeToken<ArrayList<Assignment>>() {}.type
                gson.fromJson(jsonValue, type)
            }
        })
    }

    fun deleteFile(fileId: String?): Task<Unit>? {
        return Tasks.call(mExecutor, Callable<Unit> {
            mDriveService!!.files().delete(fileId).execute()
        })
    }
}