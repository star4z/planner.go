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

    private var inProgressAssignments = ArrayList<Assignment>()
    private var completedAssignments = ArrayList<Assignment>()
    private var deletedAssignments = ArrayList<Assignment>()

    private val classNames = ArrayList<ListItem>()
    private val types = ArrayList<ListItem>()

    private val ALL_ASSIGNMENTS_FILE_NAME = "planner.assignments.all"
    private val DELETED_ASSIGNMENTS_FILE_NAME = "planner.assignments.deleted"
    private val CATEGORIES_FILE_NAME = "planner.assignments.categories"
    private val COURSES_FILE_NAME = "planner.assignments.courses"

    private val folderName = "planner_backups"


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

    internal fun readFiles(activity: Activity) {
        Log.v(TAG, "Starting read...")
        clearAssignments()
        readClasses(activity)
        readCategories(activity)
        readAssignments(activity, ALL_ASSIGNMENTS_FILE_NAME)
        readDeletedAssignments(activity)
    }

    fun readAssignments(activity: Activity, fileName: String): ArrayList<Assignment> {
        requestAppPermissions(activity)


        val root = Environment.getExternalStorageDirectory()
        val dir = File("${root.absolutePath}/${folderName}/")
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

    private fun readDeletedAssignments(activity: Activity) {
        deletedAssignments = readAssignments(activity, DELETED_ASSIGNMENTS_FILE_NAME)
    }

    private fun clearAssignments() {
        inProgressAssignments.clear()
        completedAssignments.clear()
        deletedAssignments.clear()
        types.clear()
        classNames.clear()
    }

    private fun readDataClass(activity: Activity) {

    }

    private fun readClasses(activity: Activity) {

    }

    private fun readCategories(activity: Activity) {

    }
}



