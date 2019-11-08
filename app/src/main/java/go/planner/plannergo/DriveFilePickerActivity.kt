package go.planner.plannergo

import android.os.Bundle
import android.view.Menu
import kotlinx.android.synthetic.main.toolbar.*


class DriveFilePickerActivity: DriveFileManagementActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewAdapter = ListPickerAdapter(mData, this, recyclerView)
        recyclerView.adapter = viewAdapter
    }

    override fun initToolbar() {
        toolbar.title = "Pick a file to backup from"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }
}