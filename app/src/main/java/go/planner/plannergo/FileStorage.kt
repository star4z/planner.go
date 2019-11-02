package go.planner.plannergo

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.FileNotFoundException
import java.io.PrintWriter
import java.util.*


object FileStorage {
    private val TAG = "FileStorage"

    @Throws(IllegalStateException::class, JsonSyntaxException::class)
    fun readAssignments(activity: Activity, fileUri: Uri): ArrayList<Assignment> {
        val document = DocumentFile.fromSingleUri(activity, fileUri)
        val contentResolver = activity.contentResolver
        val fileInStream = contentResolver.openInputStream(document!!.uri)!!
        val scanner = Scanner(fileInStream)
        val jsonValue = scanner.nextLine()
        scanner.close()

        val gson = Gson()
        val type = object : TypeToken<ArrayList<Assignment>>() {}.type
        val assignments: ArrayList<Assignment> = gson.fromJson(jsonValue, type)

        Log.d(TAG, "assignments=${assignments}")
        return assignments
    }

    @Throws(FileNotFoundException::class, UnsupportedOperationException::class)
    fun writeAssignments(activity: Activity, fileName: String, dir: Uri,
                         assignments: ArrayList<Assignment>) {

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
}



