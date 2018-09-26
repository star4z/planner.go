package go.planner.plannergo

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.NavigationView
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
class TrashActivity : Activity(), ColorSchemeActivity {
    private lateinit var prefs: SharedPreferences
    private lateinit var colorScheme: ColorScheme
    private var schemeSet = false

    private lateinit var mDrawerLayout: DrawerLayout

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        setColorScheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trash)

        setActionBar(toolbar)
        toolbar.title = "Trash"

        FileIO.readFiles(this)
    }

    override fun onResume() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        checkForColorSchemeUpdate()

        invalidateOptionsMenu()
        setUpNavDrawer()

        FileIO.readFiles(this)

        loadPanels()

        super.onResume()
    }

    override fun setColorScheme() {
        val scheme = prefs.getBoolean(Settings.darkMode, true)
        colorScheme = ColorScheme(scheme, this)
        setTheme(colorScheme.theme)
        Log.d(TAG, "scheme=$scheme")
    }

    override fun getColorScheme(): ColorScheme {
        return colorScheme
    }

    override fun checkForColorSchemeUpdate() {
        val newScheme = ColorScheme(prefs.getBoolean(Settings.darkMode, true), this)
        if (newScheme != colorScheme)
            recreate()
        else if (!schemeSet)
            applyColors()
    }

    override fun applyColors() {
        val navView = findViewById<NavigationView>(R.id.navigation)
        navView.setBackgroundColor(colorScheme.getColor(ColorScheme.PRIMARY))
        val coordinatorLayout = findViewById<CoordinatorLayout>(R.id.coordinator)
        coordinatorLayout.setBackgroundColor(colorScheme.getColor(ColorScheme.PRIMARY))
        if (colorScheme == ColorScheme.SCHEME_DARK) {
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.nav_color_3_bright))
            toolbar.navigationIcon.setTint(ContextCompat.getColor(this, R.color.nav_color_3_bright))
            toolbar.setBackgroundColor(colorScheme.getColor(ColorScheme.PRIMARY))
        } else {
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.textBlack))
            toolbar.navigationIcon.setTint(ContextCompat.getColor(this, R.color.textBlack))
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.nav_color_3_bright))
        }
        schemeSet = true
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

                nextView.setBackgroundColor(colorScheme.getColor(ColorScheme.ASSIGNMENT_VIEW_BG))

                val textColor = colorScheme.getColor(ColorScheme.TEXT_COLOR)

                val titleView = nextView.findViewById<TextView>(R.id.title)
                titleView.text = assignment.title
                titleView.setTextColor(textColor)
                val className = nextView.findViewById<TextView>(R.id.className)
                className.text = assignment.className
                className.setTextColor(textColor)
                nextView.findViewById<ImageView>(R.id.restore).drawable.setTint(textColor)
                nextView.findViewById<ImageView>(R.id.delete).drawable.setTint(textColor)


                nextView.findViewById<ImageView>(R.id.restore).setOnClickListener {
                    FileIO.deletedAssignments.remove(assignment)
                    FileIO.addAssignment(assignment)
                    FileIO.writeFiles(this)
                    loadPanels()
                }

                nextView.findViewById<ImageView>(R.id.delete).setOnClickListener {
                    val title = if (assignment.title == "") getString(R.string.mThis) else "'${assignment.title}'"
                    AlertDialog.Builder(this, if (colorScheme == ColorScheme.SCHEME_DARK)
                        R.style.DarkDialogTheme
                    else
                        R.style.LightDialogTheme)
                            .setTitle(R.string.perm_delete_check)
                            .setMessage("$title ${getString(R.string.gone_forever)}")
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
        header.setTextColor(colorScheme.getColor(ColorScheme.TEXT_COLOR))
        body.addView(header)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val icon = getDrawable(R.drawable.ic_trash_grey_24dp)
        icon.setTint(colorScheme.getColor(ColorScheme.TEXT_COLOR))
        menu?.getItem(0)?.icon = icon
        return super.onPrepareOptionsMenu(menu)
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
                AlertDialog.Builder(this, if (colorScheme == ColorScheme.SCHEME_DARK)
                    R.style.DarkDialogTheme
                else
                    R.style.LightDialogTheme)
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
