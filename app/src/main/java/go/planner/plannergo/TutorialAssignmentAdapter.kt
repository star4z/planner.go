package go.planner.plannergo

import android.app.Activity
import android.support.v7.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TutorialAssignmentAdapter internal constructor(dataSet: ArrayList<NewAssignment>, activity: Activity) : AssignmentItemAdapter(dataSet, 0, activity) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val a = dataSet[position]
        val vh = holder as ViewHolder
        vh.title.text = a.title
        vh.className.text = a.className

        val dateFormat = SimpleDateFormat("EEE, MM/dd/yy", Locale.US)
        vh.date.text = dateFormat.format(a.dueDate.time)
    }

}
