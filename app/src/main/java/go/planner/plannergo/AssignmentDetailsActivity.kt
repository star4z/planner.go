package go.planner.plannergo

import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_assignment.*

/**
 * Version of AssignmentActivity where old data is imported and the Assignment ID is conserved.
 */
class AssignmentDetailsActivity : AssignmentActivity() {
    private val tag = "AssignmentDetails"

    override fun manageVisibility() {
        hw_due_time.visibility =
                if (prefs.getBoolean(Settings.timeEnabled, true)) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        if (mAssignment.completed) completed_text.visibility = View.VISIBLE
    }

    override fun initViews() {
        toolbar.setTitle(R.string.assignment_details)
        hw_title.text = SpannableStringBuilder(mAssignment.title)
        hw_class.text = SpannableStringBuilder(mAssignment.className)
        hw_due_date.text = SpannableStringBuilder(dateFormat.format(mAssignment.dueDate.time))
        hw_due_time.text = SpannableStringBuilder(timeFormat.format(mAssignment.dueDate.time))
        hw_type.setSelection(mAssignment.spinnerPosition())
        hw_description.text = SpannableStringBuilder(mAssignment.description)
        is_priority.isChecked = mAssignment.priority == 1
        Log.v(tag, "notificationDate1=" + mAssignment.notificationDate1)
    }

    override fun setUpListeners() {
        dueTimePickerDialog = createTimePicker(0, hw_due_time)
        dueDatePickerDialog = createDatePicker(mAssignment.dueDate, hw_due_date)

        hw_due_date.setOnClickListener {
            Log.v(tag, "due date clicked")
            dueDatePickerDialog!!.show()
        }
        hw_due_time.setOnClickListener {
            Log.v(tag, "due time clicked")
            dueTimePickerDialog!!.show()
        }
    }

    override fun saveAssignment() {
        val mAssignment = getAssignment()

        FileIO.replaceAssignment(this, mAssignment)

        FileIO.classNames.add(mAssignment.className)
        FileIO.types.add(mAssignment.type)
    }

}