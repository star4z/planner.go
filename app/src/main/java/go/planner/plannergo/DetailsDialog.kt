package go.planner.plannergo

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.app.FragmentManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

/**
 * Displays all information about an Assignment and gives options to edit & delete it
 * Created by Ben Phillips on 1/11/2018.
 */

class DetailsDialog : DialogFragment() {
    //Assignment oldAssignment;
    private lateinit var textView: TextView
    private lateinit var classNameView: TextView
    private lateinit var dateView: TextView
    private lateinit var descriptionView: TextView
    private lateinit var typeView: TextView
    private lateinit var assignment: Assignment
    private var sortIndex: Int = 0
    private var timeEnabled: Boolean = false


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)

        assignment = Assignment(arguments)
        sortIndex = arguments.getInt("sortIndex")
        timeEnabled = arguments.getBoolean("timeEnabled")
        Log.v("DetailsDialog", "timeEnabled=" + timeEnabled)


        val view = initializeViews()

        //editButton functionality
        val editButton = view.findViewById<ImageView>(R.id.edit)
        editButton.setOnClickListener {
            val editDetailsDialog = EditDetailsDialog()
            val args = assignment.generateBundle()
            args.putInt("sortIndex", sortIndex)
            args.putBoolean("timeEnabled", timeEnabled)
            editDetailsDialog.arguments = args
            editDetailsDialog.show(fragmentManager, "DetailsDialog")
            dismiss()
        }

        //deleteButton functionality
        val deleteButton = view.findViewById<ImageView>(R.id.delete)
        deleteButton.setOnClickListener {
            val activity = activity as MainActivity
            val manager = fragmentManager

            FileIO.deleteAssignment(activity, assignment)
            activity.loadPanels(assignment, sortIndex)

            createSnackBarPopup(activity, manager)

            dismiss()
        }

        val closeButton = view.findViewById<ImageView>(R.id.close)
        closeButton.setOnClickListener { dismiss() }

        updateViews()

        builder.setView(view)

        return builder.create()

    }

    private fun initializeViews(): View {

        val inflater = activity.layoutInflater
        val view = inflater.inflate(
                R.layout.dialog_details,
                activity.findViewById<View>(android.R.id.content) as ViewGroup, false)

        textView = view.findViewById(R.id.title)
        classNameView = view.findViewById(R.id.class_name)
        dateView = view.findViewById(R.id.date)
        descriptionView = view.findViewById(R.id.description)
        typeView = view.findViewById(R.id.type)

        return view
    }

    private fun updateViews() {
        textView.text = assignment.title
        classNameView.text = assignment.className
        if (timeEnabled)
            dateView.text = SimpleDateFormat("EEE MM.dd.yyy  h:mm a", Locale.US).format(assignment.dueDate.time)
        else
            dateView.text = SimpleDateFormat("EEE MM.dd.yyyy", Locale.US).format(assignment.dueDate.time)

        descriptionView.text = assignment.description
        typeView.text = assignment.type
    }

    /**
     * SnackBar pops up to make sure user is sure they want to delete assignment.
     * Disappears after a short length of time.
     * Recreates assignment if they choose to undo.
     *
     * @param activity reference to MainActivity instance
     * @param manager  reference to current FragmentManager
     */
    private fun createSnackBarPopup(activity: MainActivity, manager: FragmentManager) {
        val title: String = if (assignment.title == "")
            "Untitled assignment"
        else
            "'" + assignment.title + "'"
        val waitDontDeleteMeYet = Snackbar.make(
                activity.findViewById(R.id.coordinator),
                title + " was deleted.",
                Snackbar.LENGTH_LONG
        )
        waitDontDeleteMeYet.setAction(R.string.undo) {
            FileIO.addAssignment(assignment)
            activity.loadPanels(assignment, sortIndex)
            val detailsDialog = DetailsDialog()
            detailsDialog.arguments = arguments
            detailsDialog.show(manager, "DetailsDialog")
        }

        waitDontDeleteMeYet.show()
    }
}
