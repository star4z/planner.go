package go.planner.plannergo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.toolbar.*

/**
 * For displaying and editing simple lists.
 */
abstract class ListActivity : AppCompatActivity(), ColorSchemeActivity {
    private val tag = "ListActivity"

    protected lateinit var recyclerView: RecyclerView
    protected lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: LinearLayoutManager
    protected lateinit var mData: ArrayList<String>

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

        mData = getData()
        Log.v(tag, "mData=$mData")

        recyclerView = findViewById(R.id.recycler_view)

        viewManager = LinearLayoutManager(this)
        viewAdapter = ListActivityAdapter(mData, this, recyclerView)



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
        for (i in 0 until menu!!.size()) {
            val item: MenuItem = menu.getItem(i)
            val s = SpannableString(item.title)
            s.setSpan(ForegroundColorSpan(colorScheme.getColor(this, Field.DG_HEAD_TEXT)), 0,
                    s.length, 0)
            item.title = s
        }
        return true
    }


    override fun onCreateView(parent: View?, name: String, context: Context,
                              attrs: AttributeSet): View? {
        if (name == "androidx.appcompat.view.menu.ListMenuItemView" &&
                parent?.parent is FrameLayout) {
            val view = parent.parent as View
            // change options menu bg color


            view.setBackgroundColor(colorScheme.getColor(this, Field.DG_BG))
        }
        return super.onCreateView(parent, name, context, attrs)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.empty_trash -> {
                AlertDialog.Builder(this, if (colorScheme == ColorScheme.SCHEME_DARK)
                    R.style.DarkDialogTheme
                else
                    R.style.LightDialogTheme)
                        .setTitle("Are you sure you want to delete all items?")
                        .setMessage("You will not be able to undo this action.")
                        .setPositiveButton("Yes") { _, _ ->
                            run {
                                val size = mData.size
                                mData.clear()
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

    open fun onRemove(position: Int) {
        FileIO.writeFiles(this)
    }

    fun addNew(@Suppress("UNUSED_PARAMETER") view: View) {
        val editText = layoutInflater.inflate(
                R.layout.dialog_edit_text,
                findViewById(android.R.id.content),
                false) as EditText
        editText.setTextColor(colorScheme.getColor(this, Field.DG_HEAD_TEXT))
        editText.setHintTextColor(colorScheme.getColor(this, Field.DG_TEXT))

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        AlertDialog.Builder(this, if (colorScheme == ColorScheme.SCHEME_DARK)
            R.style.DarkDialogTheme
        else
            R.style.LightDialogTheme)
                .setTitle(R.string.add_new)
                .setView(editText)
                .setPositiveButton(R.string.save) { _: DialogInterface, _: Int ->
                    run {
                        if (mData.contains(editText.text.toString())) {
                            val text = R.string.no_duplicates
                            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                        } else {
                            mData.add(editText.text.toString())
                            FileIO.writeFiles(this)
                            viewAdapter.notifyItemInserted(mData.size - 1)
                            listIsEmpty(false)
                        }
                        imm.hideSoftInputFromWindow(editText.windowToken, 0)
                    }
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    imm.hideSoftInputFromWindow(editText.windowToken, 0)
                }
                .show()
                .setCanceledOnTouchOutside(false)
        editText.requestFocus()
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

    }

    fun listIsEmpty(state: Boolean) {
        recycler_view_label.visibility = if (state) View.VISIBLE else View.GONE
    }

    override fun setColorScheme() {
        val isDarkMode = prefs.getBoolean(Settings.darkMode, true)
        colorScheme = if (isDarkMode) ColorScheme.SCHEME_DARK else ColorScheme.SCHEME_LIGHT
//        setTheme(colorScheme.theme)
        Log.d(tag, "scheme=$isDarkMode")
    }

    override fun getColorScheme(): ColorScheme {
        return colorScheme
    }

    override fun checkForColorSchemeUpdate() {
        val isDarkMode = prefs.getBoolean(Settings.darkMode, true)
        val newScheme = if (isDarkMode) ColorScheme.SCHEME_DARK else ColorScheme.SCHEME_LIGHT
        if (newScheme != colorScheme) {
            recreate()
        } else if (!schemeSet) {
            applyColors()
        }
    }

    override fun applyColors() {
        fab.backgroundTintList = ColorStateList.valueOf(colorScheme.getColor(this, Field.MAIN_BUTTON_BG))
        findViewById<ConstraintLayout>(R.id.parent).setBackgroundColor(colorScheme.getColor(this, Field.MAIN_BG))
        recyclerView.setBackgroundColor(colorScheme.getColor(this, Field.MAIN_BG))
        toolbar.setBackgroundColor(colorScheme.getColor(this, Field.LS_APP_BAR_BG))
        toolbar.navigationIcon = colorScheme.getDrawable(this, Field.LS_APP_BAR_BACK)
        toolbar.setTitleTextColor(colorScheme.getColor(this, Field.LS_APP_BAR_TEXT))
        toolbar.overflowIcon = colorScheme.getDrawable(this, Field.LS_APP_BAR_OPT)
        schemeSet = true
    }

    private inline fun consume(f: () -> Unit): Boolean {
        f()
        return true
    }
}
