package go.planner.plannergo

import kotlinx.android.synthetic.main.toolbar.*

/**
 * Displays all stored classes for viewing and editing
 */

/*
NOTE: Ignored classes? I.e. classes that exist in the assignments lists but the user has explicitly
said that they don't want suggested
*/
class ClassActivity : ListActivity() {

    override fun initToolbar() {
        toolbar.title = "Classes"
    }

    override fun getData(): Bag<String> {

        return FileIO.classNames
    }
}
