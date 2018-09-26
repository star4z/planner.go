package go.planner.plannergo

import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_assignment.*
import java.util.*


class NewAssignmentActivity : AssignmentActivity() {
    private val tag = "NewAssignmentActivity"

    /**
     * Handles the displaying of elements that may be hidden
     */
    override fun manageVisibility() {
        hw_due_time.visibility =
                if (prefs.getBoolean(Settings.timeEnabled, true)) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
    }

    override fun initViews() {
        hw_due_date.text = SpannableStringBuilder(dateFormat.format(mAssignment.dueDate.time))
        hw_due_time.text = SpannableStringBuilder(timeFormat.format(mAssignment.dueDate.time))
    }

    override fun setUpListeners() {
        dueTimePickerDialog = createTimePicker(0, hw_due_time)
        dueDatePickerDialog = createDatePicker(mAssignment.dueDate, hw_due_date)

        //Set default due time
        mAssignment.dueDate[Calendar.HOUR_OF_DAY] = 8
        mAssignment.dueDate[Calendar.MINUTE] = 0
        mAssignment.dueDate[Calendar.SECOND] = 0

        hw_due_date.setOnClickListener {
            Log.v("NewAssignmentActivity", "due date clicked")
            dueDatePickerDialog!!.show()
        }
        hw_due_time.setOnClickListener {
            Log.v( "NewAssignmentActivity", "due time clicked")
            dueTimePickerDialog!!.show()
        }
    }



    override fun saveAssignment() {
        val priority = if (is_priority.isChecked) 1 else 0

        val type = if (hw_type.selectedItem != null) hw_type.selectedItem.toString() else ""

        mAssignment = NewAssignment(
                hw_title.text.toString(),
                hw_class.text.toString(),
                mAssignment.dueDate,
                hw_description.text.toString(),
                false,
                type,
                priority,
                null,
                null,
                Calendar.getInstance().timeInMillis
        )

        FileIO.addAssignment(mAssignment)
        Log.v("NewAssignmentActivity","type=${mAssignment.type}")
        Log.v("NewAssignmentActivity", "$mAssignment was created.")

        FileIO.writeFiles(this)

    }

}