package go.planner.plannergo

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter

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

    private const val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 12


    fun writeAssignments(activity: Activity, fileName: String, assignments: Collection<Assignment>) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity,
                        arrayOf(Manifest.permission.READ_CONTACTS),
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            if (isExternalStorageWritable()) {

                val gson = GsonBuilder().create()
                val jsonValue = gson.toJson(assignments)

                val root = Environment.getExternalStorageDirectory()
                val dir = File("${root.absolutePath}/planner_backups")
                Log.d(TAG, "path=${root.absolutePath}")
                Log.d(TAG, "contents=${root.list()}")
                dir.mkdirs()
                val file = File(dir, "${fileName}.json")
                file.createNewFile()

                val fOut = FileOutputStream(file)
                val pWriter = PrintWriter(fOut)
                pWriter.println(jsonValue)
                pWriter.flush()
                pWriter.close()
                fOut.close()

                Log.v(TAG, "Wrote assignments to file.")
            } else {
                AlertDialog.Builder(activity)
                        .setTitle("Write storage denied!")
                        .setPositiveButton("OK") { _, _ -> }
            }
        }
    }

    fun readAssignments(c: Context, fileName: String): ArrayList<Assignment> {
        return ArrayList()
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