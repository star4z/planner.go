package go.planner.plannergo

import android.Manifest
import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.collections.ArrayList

@RunWith(AndroidJUnit4::class)
class FileStorageTest{
    @Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)


    @Before
    fun setUp() {
        Looper.prepare()
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

        FileStorage.writeAssignments(MainActivity(), fileName, assignments)
    }
}