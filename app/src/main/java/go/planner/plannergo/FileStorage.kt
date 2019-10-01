package go.planner.plannergo

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.PrintWriter
import java.nio.charset.Charset


object FileStorage {

    private val TAG = "FileStorage"

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


    private fun requestAppPermissions(activity: Activity) {
        if (hasReadPermissions(activity) && hasWritePermissions(activity)) {
            return
        }
        ActivityCompat.requestPermissions(activity, arrayOf<String?>(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ), 0) // your request code
    }

    private fun hasReadPermissions(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun hasWritePermissions(activity: Activity): Boolean {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission
                .WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun writeAssignments(activity: Activity, fileName: String, assignments: ArrayList<Assignment>) {
        requestAppPermissions(activity)

        val gson = Gson()
        val jsonValue = gson.toJson(assignments)

        val root = Environment.getExternalStorageDirectory()
        val dir = File("${root.absolutePath}/planner_backups/")
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
    }

    inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)


    fun readAssignments(activity: Activity, fileName: String): ArrayList<Assignment> {
        requestAppPermissions(activity)


        val root = Environment.getExternalStorageDirectory()
        val dir = File("${root.absolutePath}/planner_backups/")
        Log.d(TAG, "path=${root.absolutePath}")
        Log.d(TAG, "contents=${root.list()}")
        dir.mkdirs()
        val file = File(dir, "${fileName}.json")

        val fIn = FileInputStream(file)
        val data = ByteArray(file.length().toInt())
        fIn.read(data)
        fIn.close()

        val jsonValue = String(data, Charset.defaultCharset())

        val gson = Gson()

        val type = object : TypeToken<ArrayList<Assignment>>() {}.type

        val assignments: ArrayList<Assignment> = gson.fromJson<ArrayList<Assignment>>(jsonValue, type)

        Log.d(TAG, "assignments=${assignments}")

        return assignments
    }
}



