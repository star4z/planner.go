package go.planner.plannergo

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.toolbar.*

open class DriveFileManagementActivity : ListActivity() {

    companion object {
        const val OBJECTS_TO_REMOVE_KEY = "objectsToRemove"
        const val DATA_KEY = "fileNameList"
    }

    private val positionsToRemove = ArrayList<Int>()

    override fun getData(): ArrayList<String> {
        return intent.extras?.getStringArrayList(DATA_KEY)!!
    }

    override fun onEdit(oldString: String, newString: String) {}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewAdapter = ListDeletionAdapter(this, mData, recyclerView)
        recyclerView.adapter = viewAdapter
        fab.visibility = View.GONE
    }

    override fun onRemove(position: Int) {
        positionsToRemove.add(position)
    }

    override fun onBackPressed() {
        saveAndExit()
    }

    private fun saveAndExit() {
        val intent = Intent()
        intent.putIntegerArrayListExtra(OBJECTS_TO_REMOVE_KEY, positionsToRemove)
        setResult(MainActivity.RC_PICK_FILE, intent)
        finish()
    }

    override fun initToolbar() {
        toolbar.title = "Manage Drive backups"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> saveAndExit()
            R.id.empty_trash -> removeAll()
        }
        return true
    }

    override fun onRemoveAll() {
        repeat(mData.size) { positionsToRemove.add(0) }
    }
}