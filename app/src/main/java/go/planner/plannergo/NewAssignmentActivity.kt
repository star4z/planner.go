package go.planner.plannergo

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.NavUtils
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_new_assignment.*
import java.util.*
import java.util.Calendar.*

class NewAssignmentActivity : AppCompatActivity() {

    lateinit var prefs: SharedPreferences
    lateinit var assignment: NewAssignment
    var dueDate = Calendar.getInstance()
    var notifyDate = Calendar.getInstance()
    var notifyDate2 = Calendar.getInstance()
    private var n1DaysBefore = 1
    private var n2DaysBefore = 7

    private lateinit var dueTimePickerDialog: TimePickerDialog
    lateinit var dueDatePickerDialog: DatePickerDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_assignment)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        setUpListeners()

        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.p1_assignment))
        window.statusBarColor = ContextCompat.getColor(this, R.color.p1_assignment_dark)
        setSupportActionBar(toolbar)
    }

    private fun setUpListeners() {

        if (prefs.getBoolean(SettingsActivity.timeEnabled, false).not())
            hw_due_time.visibility = View.GONE

        //Set default due time
        dueDate[Calendar.HOUR_OF_DAY] = 8
        dueDate[Calendar.MINUTE] = 0
        dueDate[Calendar.SECOND] = 0

        hw_due_date.setOnClickListener {
            dueDatePickerDialog.show()
        }
        hw_due_time.setOnClickListener {
            dueTimePickerDialog.show()
        }
        n_days.setOnClickListener {
            getNumberDialog(1)
        }
        n_time.setOnClickListener {

        }
        r2_date.setOnClickListener {
            getNumberDialog(2)
        }
        r2_time.setOnClickListener {

        }

        val adapter = ArrayAdapter.createFromResource(this,
                R.array.assignment_types_array, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        hw_type.adapter = adapter

        dueTimePickerDialog = createTimePicker(dueDate)
        dueDatePickerDialog = createDatePicker(dueDate)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.new_assignment_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                consume { NavUtils.navigateUpFromSameTask(this) }
            }
            R.id.save_assignment -> {
                var priority = 0
                if (is_priority.isChecked)
                    priority = 1

                FileIO.addAssignment(NewAssignment(
                        hw_title.text.toString(),
                        hw_class.text.toString(),
                        dueDate,
                        hw_description.toString(),
                        false,
                        hw_type.selectedItem as String,
                        priority,
                        enable_custom_notification.isChecked,
                        notifyDate.timeInMillis,
                        Calendar.getInstance().timeInMillis
                ))
                FileIO.writeAssignments(this)
            }

        }
        return super.onOptionsItemSelected(item)
    }

    fun toggleCustomNotification(view: View) {
        if (enable_custom_notification.isChecked) {
            enable_custom_notification.isChecked = false
            reminder_display.visibility = View.VISIBLE
            custom_notification_2.visibility = View.VISIBLE
            if (extra_notification.isChecked) {
                extra_reminder_display.visibility = View.VISIBLE
            }
            notifyDate = dueDate.clone() as Calendar
            notifyDate[DATE] -= n_days.text.toString().toInt()

        } else {
            enable_custom_notification.isChecked = true
            reminder_display.visibility = View.GONE
            custom_notification_2.visibility = View.GONE
            extra_reminder_display.visibility = View.GONE
        }
    }


    fun toggleExtraNotification(view: View) {
        extra_notification.isChecked = !extra_notification.isChecked
        if (extra_notification.isChecked) {
            extra_reminder_display.visibility = View.VISIBLE
        } else {
            extra_reminder_display.visibility = View.GONE
        }
    }


    private fun createTimePicker(date: Calendar): TimePickerDialog {
        return TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            date[HOUR_OF_DAY] = hourOfDay
            date[MINUTE] = minute
        }, date[HOUR_OF_DAY], date[MINUTE], false)
    }

    private fun createDatePicker(date: Calendar): DatePickerDialog {
        return DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            date[YEAR] = year
            date[MONTH] = month
            date[DAY_OF_MONTH] = dayOfMonth
        }, date[YEAR], date[MONTH], date[DAY_OF_MONTH])
    }

    private fun getNumberDialog(outputTo: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Days before")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("OK") { _, _ ->
            run {
                when (outputTo) {
                    1 -> {
                        n1DaysBefore = input.text.toString().toInt()
                        n_days.setText(n1DaysBefore.toString())
                        notifyDate.set(dueDate[YEAR], dueDate[MONTH], dueDate[DATE] - n1DaysBefore)

                    }

                    2 -> {
                        n2DaysBefore = input.text.toString().toInt()
                        r2_date.setText(n2DaysBefore.toString())
                        notifyDate2.set(dueDate[YEAR], dueDate[MONTH], dueDate[DATE] - n2DaysBefore)
                    }

                    else -> Log.v("NewAssignmentActivity", "out of bounds outputTo index")
                }
            }
        }
        builder.setNegativeButton("Cancel")
        { dialog, _ -> dialog.cancel() }
    }

    private inline fun consume(f: () -> Unit): Boolean {
        f()
        return true
    }

}