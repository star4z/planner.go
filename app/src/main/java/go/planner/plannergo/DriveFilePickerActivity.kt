package go.planner.plannergo

import android.os.Bundle
import android.view.Menu
import android.view.View
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.toolbar.*


open class DriveFilePickerActivity: ListActivity() {
    companion object {
        const val DATA_KEY = "fileNameList"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewAdapter = ListPickerAdapter(mData, this, recyclerView)

        recyclerView.adapter = viewAdapter

        fab.visibility = View.GONE
    }

    override fun getData(): ArrayList<String> {
        return intent.extras?.getStringArrayList(DATA_KEY)!!
    }

    override fun initToolbar() {
        toolbar.title = "Pick a file to backup from"
    }

    override fun onEdit(oldString: String, newString: String) {
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }
}