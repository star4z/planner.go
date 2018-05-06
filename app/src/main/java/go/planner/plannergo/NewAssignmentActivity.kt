package go.planner.plannergo

import android.app.AlertDialog
import android.graphics.Color
import android.support.v4.app.NavUtils
import android.text.InputType
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ArrayAdapter
import android.widget.EditText
import java.util.*
import java.util.Calendar.*


class NewAssignmentActivity : AssignmentActivity() {
    private var n1DaysBefore = 1
    private var n2DaysBefore = 7

    /**
     * Handles the displaying of elements that may be hidden
     */
    override fun manageVisibility() {
        hw_due_time.visibility =
                if (prefs.getBoolean("pref_time_enabled", true)) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
    }

    override fun initViews() {
        hw_due_date.text = SpannableStringBuilder(dateFormat.format(assignment.dueDate.time))
        hw_due_time.text = SpannableStringBuilder(timeFormat.format(assignment.dueDate.time))
        n_time.text = SpannableStringBuilder(timeFormat.format(assignment.dueDate.time))
        r2_time.text = SpannableStringBuilder(timeFormat.format(assignment.dueDate.time))

    }

    override fun setUpListeners() {
        dueTimePickerDialog = createTimePicker(0, hw_due_time)
        dueDatePickerDialog = createDatePicker(assignment.dueDate, hw_due_date)
        notifyTimePickerDialog = createTimePicker(1, n_time)
        notifyExtraTimePickerDialog = createTimePicker(2, r2_time)

        //Set default due time
        assignment.dueDate[Calendar.HOUR_OF_DAY] = 8
        assignment.dueDate[Calendar.MINUTE] = 0
        assignment.dueDate[Calendar.SECOND] = 0

        hw_due_date.setOnClickListener {
            Log.v("NewAssignmentActivity", "due date clicked")
            dueDatePickerDialog.show()
        }
        hw_due_time.setOnClickListener {
            Log.v("NewAssignmentActivity", "due time clicked")
            dueTimePickerDialog.show()
        }
        n_days.setOnClickListener {
            getNumberDialog(1)
        }
        n_time.setOnClickListener {
            notifyTimePickerDialog.show()
        }
        r2_date.setOnClickListener {
            getNumberDialog(2)
        }
        r2_time.setOnClickListener {
            notifyExtraTimePickerDialog.show()
        }

        val adapter = ArrayAdapter.createFromResource(this,
                R.array.assignment_types_array, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        hw_type.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.new_assignment_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val color = if (ColorPicker.getColorAssignmentText() == Color.BLACK)
            R.color.textBlack else R.color.textWhite
        setToolbarMenuItemTextColor(toolbar, color, R.id.save_assignment)
        return super.onPrepareOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            android.R.id.home -> {
                consume { NavUtils.navigateUpFromSameTask(this) }
                true
            }
            R.id.save_assignment -> {
                Log.v("NewAssignmentActivity", "save button pressed")
                saveAssignment()
                consume { NavUtils.navigateUpFromSameTask(this) }
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }



    override fun saveAssignment() {
        val priority = if (is_priority.isChecked) 1 else 0
        val notification1 = if (enable_custom_notification.isChecked) assignment.notificationDate1 else null
        val notification2 = if (extra_notification.isChecked) assignment.notificationDate2 else null

        assignment = NewAssignment(
                hw_title.text.toString(),
                hw_class.text.toString(),
                assignment.dueDate,
                hw_description.text.toString(),
                false,
                hw_type.selectedItem.toString(),
                priority,
                notification1,
                notification2,
                Calendar.getInstance().timeInMillis
        )

        FileIO.addAssignment(assignment)
        Log.v("NewAssignmentActivity","type=${assignment.type}")
        Log.v("NewAssignmentActivity", "$assignment was created.")

        FileIO.writeAssignmentsToFile(this)

    }

    override fun toggleCustomNotification(@SuppressWarnings("Unu") view: View) {
        if (reminder_display.visibility == VISIBLE) {
            enable_custom_notification.isChecked = false
            reminder_display.visibility = View.GONE
            custom_notification_2.visibility = GONE
            extra_reminder_display.visibility = View.GONE
        } else {
            enable_custom_notification.isChecked = true
            reminder_display.visibility = VISIBLE
            custom_notification_2.visibility = VISIBLE
            if (extra_notification.isChecked) {
                extra_reminder_display.visibility = VISIBLE
            }
            assignment.notificationDate1 = assignment.dueDate.clone() as Calendar
            assignment.notificationDate1[DATE] -= n_days.text.toString().toInt()
        }
    }


    override fun toggleExtraNotification(@Suppress("UNUSED_PARAMETER") view: View) {
        if (extra_reminder_display.visibility == VISIBLE) {
            extra_reminder_display.visibility = GONE
            extra_notification.isChecked = false
        } else {
            extra_reminder_display.visibility = VISIBLE
            extra_notification.isChecked = true
        }
    }


    private fun getNumberDialog(outputTo: Int) {
        //TODO: create functionality
        Log.v("NewAssignmentActivity", "numberDialog triggered")
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
                        assignment.notificationDate1.set(assignment.dueDate[YEAR], assignment.dueDate[MONTH], assignment.dueDate[DATE] - n1DaysBefore)

                    }

                    2 -> {
                        n2DaysBefore = input.text.toString().toInt()
                        r2_date.setText(n2DaysBefore.toString())
                        assignment.notificationDate2.set(assignment.dueDate[YEAR], assignment.dueDate[MONTH], assignment.dueDate[DATE] - n2DaysBefore)
                    }

                    else -> print("NewAssignmentActivity: out of bounds outputTo index")
                }
            }
        }
        builder.setNegativeButton("Cancel")
        { dialog, _ -> dialog.cancel() }
        builder.create()
    }

    private inline fun consume(f: () -> Unit): Boolean {
        f()
        return true
    }

}