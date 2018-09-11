package go.planner.plannergo

import kotlinx.android.synthetic.main.toolbar.*

class TypeActivity : ListActivity() {
    override fun initToolbar() {
        toolbar.setTitle(R.string.categories)
    }

    override fun getData(): ArrayList<String> {
        return FileIO.types
    }

    override fun onEdit(oldString: String, newString: String) {
        for (i in 0 until FileIO.inProgressAssignments.size) {
            val n = FileIO.inProgressAssignments[i]
            if (n.type == oldString) {
                n.type = newString
            }
        }
        for (j in 0 until FileIO.completedAssignments.size) {
            val n = FileIO.completedAssignments[j]
            if (n.type == oldString) {
                n.type = newString
            }
        }

        val iOld = FileIO.types.indexOf(oldString)
        FileIO.types[iOld] = newString
        FileIO.writeFiles(this)
    }
}
