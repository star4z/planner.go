package go.planner.plannergo

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem

class DriveFileManagementActivity: DriveFilePickerActivity() {
    companion object {
        const val OBJECTS_TO_REMOVE_KEY = "objectsToRemove"
    }

    val positionsToRemove = ArrayList<Int>()
    lateinit var copyData: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewAdapter = ListDeletionAdapter(this, mData, recyclerView)
        recyclerView.adapter = viewAdapter

        copyData = ArrayList(getData())
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> saveAndExit()
        }
        return true
    }
}