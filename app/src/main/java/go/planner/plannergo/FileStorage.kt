package go.planner.plannergo

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*
import java.nio.charset.Charset


object FileStorage {

    private val TAG = "FileStorage"

    private val assignments = ArrayList<Assignment>()
    private val deletedAssignments = ArrayList<Assignment>()

    private val courses = ArrayList<ListItem>()
    private val categories = ArrayList<ListItem>()

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
        readCourses(activity)
        readCategories(activity)
        readAssignments(activity, ALL_ASSIGNMENTS_FILE_NAME)
        readDeletedAssignments(activity)
    }

    internal fun writeFiles(activity: Activity) {
        Log.v(TAG, "Starting write...")
        writeCourses(activity)
        writeCategories(activity)
        writeAssignments(activity, ALL_ASSIGNMENTS_FILE_NAME, assignments)
        writeDeletedAssignments(activity)
    }

    @Throws(FileNotFoundException::class)
    fun readAssignments(activity: Activity, fileName: String): ArrayList<Assignment> {

        var assignments = ArrayList<Assignment>()
        fun onFinish() {
            if (hasReadPermissions(activity) && hasWritePermissions(activity)) {
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

                assignments = gson.fromJson(jsonValue, type)

                Log.d(TAG, "assignments=${assignments}")
            } else {
                throw FileNotFoundException()
            }
        }

        val notifyingThread: NotifyingThread = object : NotifyingThread() {
            override fun doRun() {
                requestAppPermissions(activity)
            }
        }

        val listener = object : ThreadCompleteListener {
            override fun notifyOfThreadComplete(thread: Thread?) {
                Log.d(TAG, "running")
                onFinish()
            }
        }

        notifyingThread.addListener(listener)

        notifyingThread.run()

        return assignments
    }

    fun readAssignments(activity: Activity, fileUri: Uri) : ArrayList<Assignment> {
        return ArrayList()
    }

    @Throws(FileNotFoundException::class)
    fun writeAssignments(activity: Activity, fileName: String, assignments: ArrayList<Assignment>) {
        requestAppPermissions(activity)

        val gson = Gson()
        val jsonValue = gson.toJson(assignments)

        val root = Environment.getExternalStorageDirectory()
        val dir = File("${root.absolutePath}/${folderName}/")
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

    @Throws(FileNotFoundException::class, UnsupportedOperationException::class)
    fun writeAssignments(activity: Activity, fileName: String, dir: Uri,
                         assignments: ArrayList<Assignment>) {
//        requestAppPermissions(activity)

        val gson = Gson()
        val jsonValue = gson.toJson(assignments)

        val directoryDF = DocumentFile.fromTreeUri(activity, dir)
        val childDF = directoryDF?.createFile("application/json", fileName)
        val contentResolver = activity.contentResolver
        val outputStream = contentResolver.openOutputStream(childDF!!.uri)!!
        val printWriter = PrintWriter(outputStream)
        printWriter.println(jsonValue)
        printWriter.flush()
        printWriter.close()
        outputStream.close()

        Log.v(TAG, "Wrote assignments to file.")
    }

    private fun readDeletedAssignments(activity: Activity) {
        deletedAssignments.addAll(readAssignments(activity, DELETED_ASSIGNMENTS_FILE_NAME))
    }

    private fun writeDeletedAssignments(activity: Activity) {
        writeAssignments(activity, DELETED_ASSIGNMENTS_FILE_NAME, deletedAssignments)
    }

    private fun clearAssignments() {
        assignments.clear()
        deletedAssignments.clear()
        categories.clear()
        courses.clear()
    }

    private fun readListItems(activity: Activity, fileName: String): ArrayList<ListItem> {
        requestAppPermissions(activity)


        val root = Environment.getExternalStorageDirectory()
        val dir = File("${root.absolutePath}/${folderName}/")
        Log.d(TAG, "path=${root.absolutePath}")
        dir.mkdirs()
        val file = File(dir, "${fileName}.json")

        val fIn = FileInputStream(file)
        val data = ByteArray(file.length().toInt())
        fIn.read(data)
        fIn.close()

        val jsonValue = String(data, Charset.defaultCharset())

        val gson = Gson()

        val type = object : TypeToken<ArrayList<ListItem>>() {}.type

        val assignments: ArrayList<ListItem> = gson.fromJson(jsonValue, type)

        Log.d(TAG, "assignments=${assignments}")

        return assignments
    }

    private fun writeListItems(activity: Activity, fileName: String, items: ArrayList<ListItem>) {
        requestAppPermissions(activity)

        val gson = Gson()
        val jsonValue = gson.toJson(items)

        val root = Environment.getExternalStorageDirectory()
        val dir = File("${root.absolutePath}/${folderName}/")
        Log.d(TAG, "path=${root.absolutePath}")
        dir.mkdirs()
        val file = File(dir, "${fileName}.json")
        file.createNewFile()

        val fOut = FileOutputStream(file)
        val pWriter = PrintWriter(fOut)
        pWriter.println(jsonValue)
        pWriter.flush()
        pWriter.close()
        fOut.close()
    }

    private fun readCourses(activity: Activity) {
        courses.addAll(readListItems(activity, COURSES_FILE_NAME))
    }

    private fun writeCourses(activity: Activity) {
        writeListItems(activity, COURSES_FILE_NAME, courses)
    }

    private fun readCategories(activity: Activity) {
        categories.addAll(readListItems(activity, CATEGORIES_FILE_NAME))
    }

    private fun writeCategories(activity: Activity) {
        writeListItems(activity, CATEGORIES_FILE_NAME, categories)
    }

}



