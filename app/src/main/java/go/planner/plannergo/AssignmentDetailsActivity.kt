package go.planner.plannergo

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.Menu
import android.view.View
import kotlinx.android.synthetic.main.activity_assignment.*

/**
 * Version of AssignmentActivity where old data is imported and the Assignment ID is conserved.
 */
class AssignmentDetailsActivity : AssignmentActivity() {
    private val tag = "AssignmentDetailsActivity"

    override fun manageVisibility() {
        hw_due_time.visibility =
                if (prefs.getBoolean("pref_time_enabled", true)) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        if (mAssignment.completed) completed_text.visibility = View.VISIBLE
    }

    override fun initViews() {
        toolbar.title = "Assignment details"
        hw_title.text = SpannableStringBuilder(mAssignment.title)
        hw_class.text = SpannableStringBuilder(mAssignment.className)
        hw_due_date.text = SpannableStringBuilder(dateFormat.format(mAssignment.dueDate.time))
        hw_due_time.text = SpannableStringBuilder(timeFormat.format(mAssignment.dueDate.time))
        hw_type.setSelection(mAssignment.spinnerPosition())
        hw_description.text = SpannableStringBuilder(mAssignment.description)
        is_priority.isChecked = mAssignment.priority == 1
        Log.v("AssignmentDetails", "notificationDate1=" + mAssignment.notificationDate1)
    }

    override fun setUpListeners() {
        dueTimePickerDialog = createTimePicker(0, hw_due_time)
        dueDatePickerDialog = createDatePicker(mAssignment.dueDate, hw_due_date)

        hw_due_date.setOnClickListener {
            Log.v("NewAssignmentActivity", "due date clicked")
            dueDatePickerDialog!!.show()
        }
        hw_due_time.setOnClickListener {
            Log.v("NewAssignmentActivity", "due time clicked")
            dueTimePickerDialog!!.show()
        }
    }


    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val icon1 = if (ColorPicker.getColorAssignmentText() == Color.BLACK)
            R.drawable.ic_save_black_24dp else R.drawable.ic_save_white_24dp
        menu?.getItem(0)?.setIcon(icon1)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun saveAssignment() {
        val mAssignment = getAssignment()

        FileIO.replaceAssignment(this, mAssignment)

        FileIO.classNames.add(mAssignment.className)
        FileIO.types.add(mAssignment.type)
    }

}