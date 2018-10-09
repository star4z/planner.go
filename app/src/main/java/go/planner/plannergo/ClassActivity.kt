package go.planner.plannergo

import kotlinx.android.synthetic.main.toolbar.*

/**
 * Displays all stored classes for viewing and editing
 */

/*
NOTE: Add ignored classes? I.e. classes that exist in the assignments lists but the user has explicitly
said that they don't want suggested
*/
class ClassActivity : ListActivity() {

    override fun initToolbar() {
        toolbar.setTitle(R.string.classes)
    }

    override fun getData(): ArrayList<String> {
        return FileIO.classNames
    }

    override fun onEdit(oldString: String, newString: String) {
        for (i in 0 until FileIO.inProgressAssignments.size) {
            val n = FileIO.inProgressAssignments[i]
            if (n.className == oldString) {
                n.className = newString
            }
        }
        for (j in 0 until FileIO.completedAssignments.size) {
            val n = FileIO.completedAssignments[j]
            if (n.className == oldString) {
                n.className = newString
            }
        }

        val iOld = FileIO.classNames.indexOf(oldString)
        if (iOld >= 0)
            FileIO.classNames[iOld] = newString
        FileIO.writeFiles(this)
    }

}
