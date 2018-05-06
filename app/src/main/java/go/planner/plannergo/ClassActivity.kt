package go.planner.plannergo

import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class ClassActivity : ListActivity() {

    override fun initToolbar() {
        toolbar.title = "Classes"
    }

    override fun getData(): ArrayList<String> {

        return FileIO.classNames
    }
}
