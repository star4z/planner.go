package go.planner.plannergo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.toolbar.*

/**
 * For displaying and editing simple lists.
 */
abstract class ListActivity : AppCompatActivity(), ColorSchemeActivity {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: LinearLayoutManager
    private lateinit var data: ArrayList<String>

    private lateinit var prefs: SharedPreferences

    private lateinit var colorScheme: ColorScheme
    private var schemeSet = false

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        setColorScheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        initToolbar()

        setSupportActionBar(toolbar)

        FileIO.readFiles(this)

        data = getData()
        Log.v("ListActivity", "data=$data")

        recyclerView = findViewById(R.id.recycler_view)

        viewManager = LinearLayoutManager(this)
        viewAdapter = ListActivityAdapter(data, this, recyclerView)



        recyclerView.apply {

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }
    }

    override fun onResume() {
        checkForColorSchemeUpdate()
        super.onResume()
    }

    abstract fun initToolbar()

    abstract fun getData(): ArrayList<String>

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.list_menu_1, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.empty_trash -> {
                AlertDialog.Builder(this)
                        .setTitle("Are you sure you want to delete all items?")
                        .setMessage("You will not be able to undo this action.")
                        .setPositiveButton("Yes") { _, _ ->
                            run {
                                val size = data.size
                                data.clear()
                                FileIO.writeFiles(this)
                                viewAdapter.notifyItemRangeChanged(0, size)
                            }
                        }
                        .setNegativeButton("No", null)
                        .show()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> {
                consume { return super.onOptionsItemSelected(item) }
            }
        }
    }

    abstract fun onEdit(oldString: String, newString: String)

    fun addNew(@Suppress("UNUSED_PARAMETER") view: View) {
        val editText = layoutInflater.inflate(
                R.layout.dialog_edit_text,
                findViewById(android.R.id.content),
                false) as EditText
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        AlertDialog.Builder(this)
                .setTitle(R.string.add_new)
                .setView(editText)
                .setPositiveButton(R.string.save) { _: DialogInterface, _: Int ->
                    run {
                        if (data.contains(editText.text.toString())) {
                            val text = R.string.no_duplicates
                            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                        } else {
                            data.add(editText.text.toString())
                            FileIO.writeFiles(this)
                            viewAdapter.notifyItemInserted(data.size - 1)
                        }
                        imm.hideSoftInputFromWindow(editText.windowToken, 0)
                    }
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    imm.hideSoftInputFromWindow(editText.windowToken, 0)
                }
                .show()
                .setCanceledOnTouchOutside(false)
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

    }

    override fun setColorScheme() {
        val scheme = prefs.getBoolean(Settings.darkMode, true)
        colorScheme = ColorScheme(scheme, this)
        setTheme(colorScheme.theme)
        Log.d(TAG, "scheme=" + scheme!!)
    }

    override fun getColorScheme(): ColorScheme {
        return colorScheme
    }

    override fun checkForColorSchemeUpdate() {
        val scheme = prefs.getBoolean(Settings.darkMode, true)
        val newScheme = ColorScheme(scheme, this)
        if (newScheme != colorScheme) {
            recreate()
        } else if (!schemeSet) {
            applyColors()
        }
    }

    override fun applyColors() {
        fab.backgroundTintList = ColorStateList.valueOf(colorScheme.getColor(ColorScheme.ACCENT))
        findViewById<ConstraintLayout>(R.id.parent).setBackgroundColor(colorScheme.getColor(ColorScheme.PRIMARY))
        recyclerView.setBackgroundColor(colorScheme.getColor(ColorScheme.PRIMARY))
        toolbar.setBackgroundColor(colorScheme.getColor(ColorScheme.PRIMARY))
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp)
        toolbar.navigationIcon?.setTint(colorScheme.getColor(ColorScheme.TEXT_COLOR))
        toolbar.setTitleTextColor(colorScheme.getColor(ColorScheme.TEXT_COLOR))
        schemeSet = true
    }

    private inline fun consume(f: () -> Unit): Boolean {
        f()
        return true
    }
}
