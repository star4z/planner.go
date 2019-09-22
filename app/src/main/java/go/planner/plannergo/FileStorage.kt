package go.planner.plannergo

import android.app.AlertDialog
import android.content.Context
import android.os.Environment
import com.google.gson.GsonBuilder
import java.io.File

object FileStorage {

    private val TAG = "FileIO"

    internal val inProgressAssignments = ArrayList<Assignment>()
    internal val completedAssignments = ArrayList<Assignment>()
    internal val deletedAssignments = ArrayList<Assignment>()

    internal val classNames = ArrayList<String>()
    internal val types = ArrayList<String>()

    private val NEW_ASSIGNMENTS_FILE_NAME = "planner.assignments.all"
    private val DELETED_ASSIGNMENTS_FILE_NAME = "planner.assignments.deleted"
    private val TYPES_FILE_NAME = "planner.assignments.types"
    private val CLASSES_FILE_NAME = "planner.assignments.classes"

    private val folderName = "planner_backups"

    fun readAssignments(c: Context, fileName: String): ArrayList<Assignment> {
        return ArrayList()
    }

    fun writeAssignments(c: Context, fileName: String, assignments: Collection<Assignment>) {
        if (isExternalStorageWritable()) {
            val gson = GsonBuilder().create()
            val jsonValue = gson.toJson(assignments)

            val file = File(c.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), folderName)
        } else {
            AlertDialog.Builder(c)
                    .setTitle("Write storage denied!")
                    .setPositiveButton("OK") { _, _ ->  }
        }
    }

    /* Checks if external storage is available for read and write */
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /* Checks if external storage is available to at least read */
    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }
}