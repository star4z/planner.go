package go.planner.plannergo

import android.graphics.Color
import android.support.v4.app.NavUtils
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import java.util.*


class AssignmentDetailsActivity : AssignmentActivity() {

    override fun manageVisibility() {
        hw_due_time.visibility =
                if (prefs.getBoolean("pref_time_enabled", true)) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
    }

    override fun initViews() {
        toolbar.title = "Assignment details"
        hw_title.text = SpannableStringBuilder(assignment.title)
        hw_class.text = SpannableStringBuilder(assignment.className)
        hw_due_date.text = SpannableStringBuilder(dateFormat.format(assignment.dueDate.time))
        hw_due_time.text = SpannableStringBuilder(timeFormat.format(assignment.dueDate.time))
        hw_type.setSelection(assignment.spinnerPosition())
        hw_description.text = SpannableStringBuilder(assignment.description)
        is_priority.isChecked = assignment.priority == 1
//            toggleCustomNotification(View(this))
//            if (assignment.notificationDate2 != null) toggleExtraNotification(View(this))
//        }
        Log.v("AssignmentDetails", "notificationDate1=" + assignment.notificationDate1)
        n_time.text =
                if (assignment.notificationDate1 != null)
                    SpannableStringBuilder(timeFormat.format(assignment.notificationDate1.time))
                else
                    SpannableStringBuilder("8:00 am")
        r2_time.text =
                if (assignment.notificationDate2 != null)
                    SpannableStringBuilder(timeFormat.format(assignment.notificationDate2.time))
                else
                    SpannableStringBuilder("8:00 am")
    }

    override fun setUpListeners() {
        dueTimePickerDialog = createTimePicker(0, hw_due_time)
        dueDatePickerDialog = createDatePicker(assignment.dueDate, hw_due_date)
        notifyTimePickerDialog = createTimePicker(1, n_time)
        notifyExtraTimePickerDialog = createTimePicker(2, r2_time)

        hw_due_date.setOnClickListener {
            Log.v("NewAssignmentActivity", "due date clicked")
            dueDatePickerDialog.show()
        }
        hw_due_time.setOnClickListener {
            Log.v("NewAssignmentActivity", "due time clicked")
            dueTimePickerDialog.show()
        }
        n_days.setOnClickListener {
            //            getNumberDialog(1)
        }
        n_time.setOnClickListener {
            notifyTimePickerDialog.show()
        }
        r2_date.setOnClickListener {
            //            getNumberDialog(2)
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
        menuInflater.inflate(R.menu.assignment_details_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val icon1 = if (ColorPicker.getColorAssignmentText() == Color.BLACK)
            R.drawable.ic_save_black_24dp else R.drawable.ic_save_white_24dp
        val icon2 = if (ColorPicker.getColorAssignmentText() == Color.BLACK)
            R.drawable.ic_delete_black_24dp else R.drawable.ic_delete_white_24dp

        menu?.getItem(0)?.setIcon(icon2)
        menu?.getItem(1)?.setIcon(icon1)
        return super.onPrepareOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save_assignment -> {
                saveAssignment()
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
            R.id.delete -> {
                FileIO.deleteAssignment(this, assignment)
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun toggleCustomNotification(@Suppress("UNUSED_PARAMETER") view: View) {
        Log.v("NewAssignmentActivity", "Toggling custom notif")
        if (reminder_display.visibility == View.VISIBLE) {
            enable_custom_notification.isChecked = false
            reminder_display.visibility = View.GONE
            custom_notification_2.visibility = View.GONE
            extra_reminder_display.visibility = View.GONE
        } else {
            enable_custom_notification.isChecked = true
            reminder_display.visibility = View.VISIBLE
            custom_notification_2.visibility = View.VISIBLE
            if (extra_notification.isChecked) {
                extra_reminder_display.visibility = View.VISIBLE
            }
            val notifyDate = assignment.dueDate.clone() as Calendar
            notifyDate[Calendar.DATE] -= n_days.text.toString().toInt()
            assignment.notificationDate1 = notifyDate
        }
    }

    override fun toggleExtraNotification(@Suppress("UNUSED_PARAMETER") view: View) {
        if (extra_reminder_display.visibility == View.VISIBLE) {
            extra_reminder_display.visibility = View.GONE
            extra_notification.isChecked = false
        } else {
            extra_reminder_display.visibility = View.VISIBLE
            extra_notification.isChecked = true
        }
    }

    override fun saveAssignment() {

        val mTitl = hw_title.text.toString()
        val mClas = hw_class.text.toString()
        val mDate = assignment.dueDate
        val mDesc = hw_description.text.toString()
        val mComp = assignment.completed
        val mType = hw_type.selectedItem.toString()
        val mPrio = if (is_priority.isChecked) 1 else 0

        //TODO: is not saving Dates properly
        val notification1 = if (enable_custom_notification.isChecked) assignment.notificationDate1 else null
        val notification2 = if (extra_notification.isChecked) assignment.notificationDate2 else null
        Log.v("AssignmentDetails", "notificationDate1=$notification1")
//
//        val notifCalendar2 = assignment.dueDate
//        notifCalendar2.add(Calendar.DAY_OF_MONTH, -r2_date.text.toString().toInt())
//        timeStr = r2_time.text.split(":")
//        minute = timeStr[1].substring(0, 2).toInt()
//        notifCalendar2[Calendar.HOUR_OF_DAY] = timeStr[0].toInt()
//        notifCalendar2[Calendar.MINUTE] = minute
//        val mNot2 = notifCalendar2.timeInMillis

        val mUID = assignment.uniqueID

        val mAssignment = NewAssignment(mTitl, mClas, mDate, mDesc, mComp, mType, mPrio, notification1, notification2, mUID)

        FileIO.replaceAssignment(this, mAssignment)
    }

}