package go.planner.plannergo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import kotlinx.android.synthetic.main.activity_assignment.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.*

abstract class AssignmentActivity : AppCompatActivity() {
    //Reads and writes involve this object
    internal lateinit var assignment: NewAssignment
    //    Calendar notifyDate = Calendar.getInstance();
    //    Calendar notifyDate2 = Calendar.getInstance();

    //Settings file
    internal lateinit var prefs: SharedPreferences


    //Listeners
    internal var dueTimePickerDialog: TimePickerDialog? = null
    internal var dueDatePickerDialog: DatePickerDialog? = null
    internal var notifyTimePickerDialog: TimePickerDialog? = null
    internal var notifyExtraTimePickerDialog: TimePickerDialog? = null

    internal var dateFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US)
    internal var timeFormat = SimpleDateFormat("h:mm a".toLowerCase(), Locale.US)

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

        assignment = FileIO.getAssignment(if (thisIntent.extras != null) thisIntent.extras!!.getLong("uniqueID", -1L) else -1L)
        Log.v("AssignmentActivity", "assignment=$assignment")


        val layoutID = android.R.layout.simple_dropdown_item_1line
        val classArrayList = FileIO.classNames
        val classes = classArrayList.toTypedArray()
        val classAdapter = ArrayAdapter(this, layoutID, classes)
        hw_class.setAdapter(classAdapter)
        hw_class.threshold = 0


        val typesArrayList = FileIO.types
        val types = typesArrayList.toTypedArray()
        val typesAdapter = ArrayAdapter(this, layoutID, types)
        //
        //        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        //                R.array.assignment_types_array, android.R.layout.simple_spinner_item);
        //        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        val typeSpinner = findViewById<Spinner>(R.id.hw_type)
        typeSpinner.adapter = typesAdapter

        manageVisibility()
        setUpListeners()
        initViews()
        initToolbar()
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
                navigateUpTo(Intent(this, MainActivity::class.java))
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
            0 -> calendar = if (assignment.dueDate == null)
                Calendar.getInstance()
            else
                assignment.dueDate
            1 -> calendar = if (assignment.notificationDate1 == null)
                Calendar.getInstance()
            else
                assignment.notificationDate1
            2 -> calendar = if (assignment.notificationDate2 == null)
                Calendar.getInstance()
            else
                assignment.notificationDate2
            else -> calendar = if (assignment.dueDate == null)
                Calendar.getInstance()
            else
                assignment.dueDate
        }

        return TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { timePicker, hourOfDay, minute ->
            calendar.set(HOUR_OF_DAY, hourOfDay)
            calendar.set(MINUTE, minute)
            view.setText(timeFormat.format(calendar.time))
            when (dateToModify) {
                0 -> assignment.dueDate = calendar
                1 -> assignment.notificationDate1 = calendar
                2 -> assignment.notificationDate2 = calendar
                else -> assignment.dueDate = calendar
            }
        }, calendar.get(HOUR_OF_DAY), calendar.get(MINUTE), false)

    }

    /**
     * reads and writes to date
     * outputs changes to view
     */
    internal fun createDatePicker(date: Calendar, view: EditText): DatePickerDialog {
        return DatePickerDialog(this, DatePickerDialog.OnDateSetListener { datePicker, year, month, dayOfMonth ->
            date.set(YEAR, year)
            date.set(MONTH, month)
            date.set(DAY_OF_MONTH, dayOfMonth)
            view.setText(dateFormat.format(date.time))
        }, date.get(YEAR), date.get(MONTH), date.get(DAY_OF_MONTH))
    }

    abstract fun toggleCustomNotification(view: View)

    abstract fun toggleExtraNotification(view: View)

    fun togglePriorityAssignment(view: View) {
        if (assignment.priority == 1) {
            assignment.priority = 0
            is_priority.isChecked = false
        } else {
            assignment.priority = 1
            is_priority.isChecked = true
        }
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
