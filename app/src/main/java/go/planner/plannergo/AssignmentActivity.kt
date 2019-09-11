package go.planner.plannergo

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import kotlinx.android.synthetic.main.activity_assignment.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.*

abstract class AssignmentActivity : AppCompatActivity(), ColorSchemeActivity {
    private val tag = "AssignmentActivity"

    //Keeps old data in case edits were accidental
    private lateinit var oldAssignment: Assignment
    //Reads and writes involve this object
    internal lateinit var mAssignment: Assignment

    //Settings file
    internal lateinit var prefs: SharedPreferences
    internal lateinit var colorScheme: ColorScheme
    private var schemeSet = false

    //Listeners
    internal var dueTimePickerDialog: TimePickerDialog? = null
    internal var dueDatePickerDialog: DatePickerDialog? = null

    internal var dateFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US)
    internal var timeFormat = SimpleDateFormat("h:mm a".toLowerCase(Locale.US), Locale.US)

    private lateinit var typeSpinner: Spinner
    private var layoutID = 0

    /**
     * implementation should include super.onCreate(savedInstanceState;
     * and setContentView(R.layout.activity_assignment);
     *
     * @param savedInstanceState for restoring state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        setColorScheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignment)


        val thisIntent = intent

        mAssignment = FileIO.getAssignment(if (thisIntent.extras != null) thisIntent.extras!!.getLong("uniqueID", -1L) else -1L)
        Log.v("AssignmentActivity", "mAssignment=$mAssignment")
        oldAssignment = Assignment(mAssignment)

        //Set up autocomplete for class field
        val classArrayList = FileIO.classNames
        val classes = classArrayList.toTypedArray()

        layoutID = if (colorScheme == ColorScheme.SCHEME_DARK)
            R.layout.spinner_item_dark
        else
            android.R.layout.simple_dropdown_item_1line
        val classAdapter = ArrayAdapter(this, layoutID, classes)


        hw_class.setAdapter(classAdapter)
        hw_class.threshold = 0

        updateTypeSpinner()

        manageVisibility()
        setUpListeners()
        initViews()
        setSupportActionBar(toolbar)
    }

    override fun onResume() {
        checkForColorSchemeUpdate()
        super.onResume()
    }

    /**
     * Fills spinner with options from FileIO.types
     */
    private fun updateTypeSpinner() {
        val typesArrayList = FileIO.types
        val types = typesArrayList.toTypedArray()

        layoutID = if (colorScheme == ColorScheme.SCHEME_DARK)
            R.layout.spinner_item_dark
        else
            android.R.layout.simple_dropdown_item_1line

        val typesAdapter = ArrayAdapter(this, layoutID, types)
        typeSpinner = findViewById(R.id.hw_type)
        typeSpinner.adapter = typesAdapter
    }


    /**
     * Determines which views should be visible when the activity starts.
     */
    internal abstract fun manageVisibility()

    internal abstract fun initViews()

    internal abstract fun setUpListeners()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.new_assignment_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.getItem(0)?.icon = colorScheme.getDrawable(this, Field.AS_APP_BAR_OPT)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save_assignment -> {
                Log.v("NewAssignmentActivity", "save button pressed")
                saveAssignment()
                navigateUpTo(Intent(this, MainActivity::class.java))
                true
            }
            android.R.id.home -> {
                saveCheckAndNavigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        saveCheckAndNavigateUp()
    }

    private fun saveCheckAndNavigateUp() {
        Log.d(tag, "oldAssignment == assignment?${oldAssignment.compareFields(getAssignment())}")
        if (oldAssignment.compareFields(getAssignment()))
            navigateUpTo(Intent(this, MainActivity::class.java))
        else {
            //TODO: add don't ask me again option
            val style = if (colorScheme == ColorScheme.SCHEME_DARK)
                R.style.DarkDialogTheme
            else
                R.style.LightDialogTheme
            AlertDialog.Builder(this, style)
                    .setTitle(R.string.do_not_save)
                    .setMessage(R.string.changes_wont_be_saved)
                    .setNegativeButton(R.string.leave) { _, _ ->
                        navigateUpTo(Intent(this, MainActivity::class.java))
                    }
                    .setNeutralButton(R.string.leave_and_save) { _, _ ->
                        saveAssignment()
                        navigateUpTo(Intent(this, MainActivity::class.java))
                    }
                    .setPositiveButton(R.string.stay, null)
                    .create().show()
        }
    }

    /**
     * creates dialog to modify a date field
     *
     * @param dateToModify 0 for dueDate, 1 for notificationDate1, 2 for NotificationDate2
     * @param view         field to modify
     * @return TimePickerDialog
     */
    internal fun createTimePicker(dateToModify: Int, view: EditText): TimePickerDialog {
        val calendar: Calendar
        when (dateToModify) {
            0 -> calendar =
                    if (mAssignment.dueDate == null)
                        getInstance()
                    else
                        mAssignment.dueDate
            1 -> calendar =
                    if (mAssignment.notificationDate1 == null)
                        getInstance()
                    else
                        mAssignment.notificationDate1
            2 -> calendar =
                    if (mAssignment.notificationDate2 == null)
                        getInstance()
                    else
                        mAssignment.notificationDate2
            else -> calendar =
                    if (mAssignment.dueDate == null)
                        getInstance()
                    else
                        mAssignment.dueDate
        }

        return TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(HOUR_OF_DAY, hourOfDay)
            calendar.set(MINUTE, minute)
            view.setText(timeFormat.format(calendar.time))
            when (dateToModify) {
                0 -> mAssignment.dueDate = calendar
                1 -> mAssignment.notificationDate1 = calendar
                2 -> mAssignment.notificationDate2 = calendar
                else -> mAssignment.dueDate = calendar
            }
        }, calendar.get(HOUR_OF_DAY), calendar.get(MINUTE), false)

    }

    /**
     * reads and writes to date
     * outputs changes to view
     */
    internal fun createDatePicker(date: Calendar, view: EditText): DatePickerDialog {
        return DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            date.set(YEAR, year)
            date.set(MONTH, month)
            date.set(DAY_OF_MONTH, dayOfMonth)
            view.setText(dateFormat.format(date.time))
        }, date.get(YEAR), date.get(MONTH), date.get(DAY_OF_MONTH))
    }

    fun openTypeActivity(@Suppress("UNUSED_PARAMETER") view: View) {
        startActivityForResult(Intent(this, TypeActivity::class.java), 0)
    }

    /**Updates type spinner when user returns to activity from type editor*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        updateTypeSpinner()
    }

    internal fun getAssignment(): Assignment {
        val mTitle = hw_title.text.toString()
        val mClass = hw_class.text.toString()
        val mDate = mAssignment.dueDate
        val mDesc = hw_description.text.toString()
        val mComp = mAssignment.completed
        val mType = if (hw_type.selectedItem != null) hw_type.selectedItem.toString() else ""
        val priority = if (is_priority.isChecked) 1 else 0
        val mUID = mAssignment.uniqueID

        return Assignment(mTitle, mClass, mDate, mDesc, mComp, mType, priority, null, null, mUID)
    }

    internal abstract fun saveAssignment()

    override fun setColorScheme() {
        val darkmode = prefs.getBoolean(Settings.darkMode, true)
        colorScheme = if (darkmode) ColorScheme.SCHEME_DARK else ColorScheme.SCHEME_LIGHT
//        setTheme(colorScheme.theme)
        Log.d(tag, "darkmode=$darkmode")
    }

    override fun getColorScheme(): ColorScheme {
        return colorScheme
    }

    override fun checkForColorSchemeUpdate() {
        val darkmode = prefs.getBoolean(Settings.darkMode, true)
        val newScheme = if (darkmode) ColorScheme.SCHEME_DARK else ColorScheme.SCHEME_LIGHT
        if (newScheme != colorScheme) {
            recreate()
        } else if (!schemeSet) {
            applyColors()
        }
    }

    override fun applyColors() {

        val textColor = colorScheme.getColor(this, Field.AS_TEXT)

        parentView.setBackgroundColor(colorScheme.getColor(this, Field.MAIN_BG))

        toolbar.setBackgroundColor(colorScheme.getColor(this, Field.AS_APP_BAR_BG))
        toolbar.setTitleTextColor(colorScheme.getColor(this, Field.AS_APP_BAR_TEXT))
        toolbar.navigationIcon = colorScheme.getDrawable(this, Field.AS_APP_BAR_BACK)

        var view: View
        for (i in 0 until constraint_layout.childCount) {
            view = constraint_layout.getChildAt(i)
            (view as? TextView)?.setTextColor(textColor)
            if (view is EditText) {
                view.setTextColor(textColor)
                view.setHintTextColor(ColorUtils.blendARGB(textColor,
                        if (colorScheme == ColorScheme.SCHEME_DARK) Color.BLACK else Color.WHITE,
                        0.5f)
                )
            }
            (view as? AutoCompleteTextView)?.setTextColor(textColor)
            (view as? ImageView)?.imageTintList = ColorStateList.valueOf(textColor)

            val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
            )

            val csl = ColorStateList(
                    states,
                    intArrayOf(
                            colorScheme.getColor(this, Field.AS_CHECK_ON),
                            colorScheme.getColor(this, Field.AS_CHECK_OFF)
                    )
            )
            (view as? CheckBox)?.buttonTintList = csl
        }
        schemeSet = true
    }

}
