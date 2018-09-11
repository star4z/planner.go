package go.planner.plannergo

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.annotation.ColorRes
import android.support.annotation.IdRes
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.menu.ActionMenuItemView
import android.support.v7.widget.ActionMenuView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import go.planner.plannergo.FileIO.getAssignment
import kotlinx.android.synthetic.main.activity_assignment.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.*

abstract class AssignmentActivity : AppCompatActivity() {
    private val tag = "AssignmentActivity"

    //Keeps old data in case edits were accidental
    private lateinit var oldAssignment: NewAssignment
    //Reads and writes involve this object
    internal lateinit var mAssignment: NewAssignment

    //Settings file
    internal lateinit var prefs: SharedPreferences

    //Listeners
    internal var dueTimePickerDialog: TimePickerDialog? = null
    internal var dueDatePickerDialog: DatePickerDialog? = null

    internal var dateFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US)
    internal var timeFormat = SimpleDateFormat("h:mm a".toLowerCase(), Locale.US)

    private lateinit var typeSpinner: Spinner
    private val layoutID = android.R.layout.simple_dropdown_item_1line


    /**
     * implementation should include super.onCreate(savedInstanceState;
     * and setContentView(R.layout.activity_assignment);
     *
     * @param savedInstanceState for restoring state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignment)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        val thisIntent = intent

        mAssignment = FileIO.getAssignment(if (thisIntent.extras != null) thisIntent.extras!!.getLong("uniqueID", -1L) else -1L)
        Log.v("AssignmentActivity", "mAssignment=$mAssignment")
        oldAssignment = mAssignment.clone()

        //Set up autocomplete for class field
        val classArrayList = FileIO.classNames
        val classes = classArrayList.toTypedArray()
        val classAdapter = ArrayAdapter(this, layoutID, classes)
        hw_class.setAdapter(classAdapter)
        hw_class.threshold = 0

        updateTypeSpinner()

        manageVisibility()
        setUpListeners()
        initViews()
        initToolbar()
    }

    /**
     * Fills spinner with options from FileIO.types
     */
    private fun updateTypeSpinner() {
        val typesArrayList = FileIO.types
        val types = typesArrayList.toTypedArray()
        val typesAdapter = ArrayAdapter(this, layoutID, types)
        typeSpinner = findViewById(R.id.hw_type)
        typeSpinner.adapter = typesAdapter
    }


    private fun initToolbar() {
        val appBar = findViewById<Toolbar>(R.id.toolbar)
        appBar.setBackgroundColor(ColorPicker.getColorAssignment())
        appBar.setTitleTextColor(ColorPicker.getColorAssignmentText())
        val menuIcon: Drawable? = if (ColorPicker.getColorAssignmentText() == Color.BLACK) {
            appBar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
            ContextCompat.getDrawable(applicationContext,
                    R.drawable.ic_more_vert_black_24dp)
        } else {
            appBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
            ContextCompat.getDrawable(applicationContext,
                    R.drawable.ic_more_vert_white_24dp)
        }
        appBar.overflowIcon = menuIcon
        window.statusBarColor = ColorPicker.getColorAssignmentAccent()
        setSupportActionBar(appBar)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save_assignment -> {
                Log.v("NewAssignmentActivity", "save button pressed")
                saveAssignment()
                navigateUpTo(Intent(this, MainActivity::class.java))
                true
            }
            android.R.id.home -> {
                Log.d(tag, "oldAssignment == assignment?${oldAssignment.compareFields(getAssignment())}")
                if (oldAssignment.compareFields(getAssignment()))
                    navigateUpTo(Intent(this, MainActivity::class.java))
                else {
                    //TODO: add don't ask me again option
                    AlertDialog.Builder(this)
                            .setTitle(R.string.do_not_save)
                            .setMessage(R.string.changes_wont_be_saved)
                            .setPositiveButton(R.string.leave) { _, _ ->
                                navigateUpTo(Intent(this, MainActivity::class.java))
                            }
                            .setNegativeButton(R.string.stay, null)
                            .create().show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
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
                        Calendar.getInstance()
                    else
                        mAssignment.dueDate
            1 -> calendar =
                    if (mAssignment.notificationDate1 == null)
                        Calendar.getInstance()
                    else
                        mAssignment.notificationDate1
            2 -> calendar =
                    if (mAssignment.notificationDate2 == null)
                        Calendar.getInstance()
                    else
                        mAssignment.notificationDate2
            else -> calendar =
                    if (mAssignment.dueDate == null)
                        Calendar.getInstance()
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

    internal fun getAssignment():NewAssignment{
        val mTitl = hw_title.text.toString()
        val mClas = hw_class.text.toString()
        val mDate = mAssignment.dueDate
        val mDesc = hw_description.text.toString()
        val mComp = mAssignment.completed
        val mType = if (hw_type.selectedItem != null) hw_type.selectedItem.toString() else ""
        val mPrio = if (is_priority.isChecked) 1 else 0
        val mUID = mAssignment.uniqueID

        return NewAssignment(mTitl, mClas, mDate, mDesc, mComp, mType, mPrio, null, null, mUID)
    }

    internal abstract fun saveAssignment()

    companion object {

        fun setToolbarMenuItemTextColor(toolbar: Toolbar?, @ColorRes color: Int, @IdRes resId: Int) {
            if (toolbar != null) {
                for (i in 0 until toolbar.childCount) {
                    val view = toolbar.getChildAt(i)
                    if (view is ActionMenuView) {
// view children are accessible only after layout-ing
                        view.post {
                            for (j in 0 until view.childCount) {
                                val innerView = view.getChildAt(j)
                                if (innerView is ActionMenuItemView) {
                                    if (resId == innerView.id) {
                                        innerView.setTextColor(ContextCompat.getColor(toolbar.context, color))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
