package go.planner.plannergo

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_trash.*

class TrashActivity : Activity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var mDrawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trash)

        setActionBar(toolbar)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.textWhite))

        FileIO.readAssignmentsFromFile(this)
    }

    override fun onResume() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        ColorPicker.setColors(this)
        invalidateOptionsMenu()
        setUpNavDrawer()

        FileIO.readAssignmentsFromFile(this)

        loadPanels()

        super.onResume()
    }

    private fun setUpNavDrawer() {
        val drawerOptions = resources.getStringArray(R.array.drawer_options_array)
        val tArray = resources.obtainTypedArray(R.array.drawer_icons_array)
        val count = tArray.length()
        val drawerIcons = IntArray(count)
        for (i in drawerIcons.indices) {
            drawerIcons[i] = tArray.getResourceId(i, 0)
        }
        tArray.recycle()

        mDrawerLayout = findViewById(R.id.drawer_layout)
        val mDrawerList = findViewById<ListView>(R.id.drawer_list)

        mDrawerList.adapter = DrawerAdapter(this, drawerOptions, drawerIcons)


        mDrawerList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("mode_InProgress", true)
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    finish()
                    startActivity(intent)
                }
                1 -> {
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("mode_InProgress", true)
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    finish()
                    startActivity(intent)
                }
                2 -> loadPanels()
                3 -> startActivity(Intent(this@TrashActivity, SettingsActivity::class.java))
            }
            mDrawerLayout.closeDrawers()
        }
    }

    private fun loadPanels() {
        body.removeAllViews()

        addHeading("Assignments in the trash are permanently deleted after 30 days.")
        if (FileIO.deletedAssignments.isEmpty())
            addHeading("The trash is empty.")
        else {
            for (assignment: NewAssignment in FileIO.deletedAssignments) {
                val nextView = layoutInflater.inflate(
                        R.layout.view_deleted_item,
                        findViewById(android.R.id.content),
                        false
                )
                nextView.findViewById<TextView>(R.id.textView).text = assignment.title
                nextView.findViewById<TextView>(R.id.textView3).text = assignment.className


                nextView.findViewById<ImageView>(R.id.restore).setOnClickListener {
                    FileIO.deletedAssignments.remove(assignment)
                    FileIO.addAssignment(assignment)
                    FileIO.writeAssignmentsToFile(this)
                    loadPanels()
                }


                nextView.findViewById<ImageView>(R.id.delete).setOnClickListener {
                    AlertDialog.Builder(this)
                            .setTitle("Permanently delete this?")
                            .setMessage("'${assignment.title}' will be gone forever.")
                            .setPositiveButton("Yep", DialogInterface.OnClickListener { dialog, which ->
                                run {
                                    FileIO.deletedAssignments.remove(assignment)
                                    FileIO.writeAssignmentsToFile(this)
                                    loadPanels()
                                }
                            })
                            .setNegativeButton("No", null)
                            .show()
                }
                body.addView(nextView)
            }
        }
        addHeading(" ")
    }

    private fun addHeading(inText: String) {
        val outText = if (inText == "") "Untitled" else inText
        val header = layoutInflater.inflate(
                R.layout.view_sort_header,
                findViewById<View>(android.R.id.content) as ViewGroup,
                false
        ) as TextView
        header.text = outText
        body.addView(header)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                mDrawerLayout.openDrawer(Gravity.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
