package go.planner.plannergo

import android.app.Activity
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TutorialAssignmentAdapter internal constructor(dataSet: ArrayList<Assignment>, activity: Activity) : AssignmentItemAdapter(dataSet, 0, activity) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val a = dataSet[position]
        val vh = holder as ViewHolder
        vh.title.text = a.title
        vh.className.text = a.className

        if (a.priority > 0) {
            vh.priorityDot.visibility = View.VISIBLE
            vh.priorityDot.setImageResource(R.drawable.ic_priority_dot_on_24dp)
        } else {
            vh.priorityDot.visibility = View.GONE
            vh.priorityDot.setImageResource(R.drawable.ic_priority_dot_off_24dp)
        }

        val dateFormat = SimpleDateFormat("EEE, MM/dd/yy", Locale.US)
        vh.date.text = dateFormat.format(a.dueDate.time)
    }

}
