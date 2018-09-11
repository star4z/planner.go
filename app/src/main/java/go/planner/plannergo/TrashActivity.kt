package go.planner.plannergo

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_trash.*

/**
 * Displays deleted assignments. Does not do any sorting in order to handle larger quantities of
 * assignments than would be expected in MainActivity, for example.
 */
class TrashActivity : Activity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var mDrawerLayout: DrawerLayout

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trash)

        setActionBar(toolbar)
        toolbar.title = "Trash"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            toolbar.overflowIcon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_more_vert_white_24dp)

        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.textWhite))

        FileIO.readFiles(this)
    }

    override fun onResume() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        ColorPicker.setColors(this)
        invalidateOptionsMenu()
        setUpNavDrawer()

        FileIO.readFiles(this)

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

        mDrawerList.adapter = DrawerAdapter(this, drawerOptions, drawerIcons, 3)


        mDrawerList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            when (position) {
                1 -> {
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("mode_InProgress", true)
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    finish()
                    startActivity(intent)
                }
                2 -> {
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("mode_InProgress", false)
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    finish()
                    startActivity(intent)
                }
                3 -> loadPanels()
                4 -> startActivity(Intent(this@TrashActivity, SettingsActivity::class.java))
                5 -> startActivity(Intent(this@TrashActivity, FeedbackActivity::class.java))
            }
            mDrawerLayout.closeDrawers()
        }
    }

    private fun loadPanels() {
        body.removeAllViews()
        Log.v("TrashActivity", FileIO.deletedAssignments.toString())

        addHeading(R.string.deletion_is_permanent)
        if (FileIO.deletedAssignments.isEmpty())
            addHeading(R.string.trash_is_empty)
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
                    FileIO.writeFiles(this)
                    loadPanels()
                }

                nextView.findViewById<ImageView>(R.id.delete).setOnClickListener {
                    title = if (assignment.title == "") getString(R.string.mThis) else "'${assignment.title}'"
                    AlertDialog.Builder(this)
                            .setTitle(R.string.perm_delete_check)
                            .setMessage("$title ${R.string.gone_forever}")
                            .setPositiveButton(R.string.delete) { _, _ ->
                                run {
                                    FileIO.deletedAssignments.remove(assignment)
                                    FileIO.writeFiles(this)
                                    loadPanels()
                                }
                            }
                            .setNegativeButton(R.string.keep, null)
                            .show()
                }
                body.addView(nextView)
            }
        }
    }

    private fun addHeading(inText: Int) {
        val header = layoutInflater.inflate(
                R.layout.view_sort_header,
                findViewById<View>(android.R.id.content) as ViewGroup,
                false
        ) as TextView
        header.setText(inText)
        body.addView(header)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.trash_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                mDrawerLayout.openDrawer(Gravity.START)
                return true
            }
            R.id.empty_trash -> {
                AlertDialog.Builder(this)
                        .setTitle(R.string.empty_trash_check)
                        .setMessage(R.string.no_undo)
                        .setPositiveButton(R.string.yes) { _, _ ->
                            run {
                                FileIO.deletedAssignments.clear()
                                FileIO.writeFiles(this)
                                loadPanels()
                            }
                        }
                        .setNegativeButton(R.string.no, null)
                        .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
