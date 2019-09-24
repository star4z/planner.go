package go.planner.plannergo

import android.app.Activity
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.collections.ArrayList

@RunWith(AndroidJUnit4::class)
class FileStorageTest{
    @Before
    fun setUp() {

    }


    @Test
    fun testWriteAssignments(){
        // Context of the app under test.
        val appContext: Context = ApplicationProvider.getApplicationContext()

        val fileName = "testFile"

        val assignments = ArrayList<Assignment>()

        for (i in 0..2) {
            assignments.add(Assignment(
                    "title$i",
                    "class$i",
                    Calendar.getInstance(),
                    "description$i",
                    false,
                    "type$i",
                    0,
                    null,
                    null,
                    i.toLong()
            ))
        }

        FileStorage.writeAssignments(Activity(), fileName, assignments)
    }

    @Test
    fun testIsExternalStorageWriteable() {
        assert(FileStorage.isExternalStorageWritable())
    }

    @Test
    fun testIsExternalStorageReadable() {
        assert(FileStorage.isExternalStorageReadable())
    }
}