package go.planner.plannergo;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Stores oldAssignment GUI info
 * Handles checkbox and body OnClickListeners
 * <p>
 * Created by Ben Phillips on 12/22/2017.
 */

class AssignmentViewWrapper implements Comparable<Object> {

    LinearLayout container;

    private TextView titleView, classView, dateView;
    private int sortIndex;

    private final Assignment assignment;

    AssignmentViewWrapper(final Activity activity, Assignment newAssignment, int sortIndex) {
        FragmentManager f = activity.getFragmentManager();
        assignment = newAssignment;
        this.sortIndex = sortIndex;

        LayoutInflater inflater = activity.getLayoutInflater();
        ViewGroup parent = activity.findViewById(android.R.id.content);
        container = (LinearLayout) inflater.inflate(R.layout.view_assignment, parent, false);

        RelativeLayout body = container.findViewById(R.id.body);
        CheckBox checkBox = container.findViewById(R.id.checkbox);
        titleView = container.findViewById(R.id.title);
        classView = container.findViewById(R.id.class_name);
        dateView = container.findViewById(R.id.date);

        checkBox.setChecked(assignment.completed);

        updateData((MainActivity) activity);

        //click functionality (opens DetailsDialog)
        body.setOnClickListener(new BodyClickListener(newAssignment, (MainActivity) activity, f));
        //checkBox functionality
        checkBox.setOnCheckedChangeListener(   new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    final MainActivity mainActivity = (MainActivity) activity;
                    ArrayList<Assignment> currentAssignments;
                    if (assignment.completed)
                        currentAssignments = FileIO.completedAssignments;
                    else
                        currentAssignments = FileIO.inProgressAssignments;
                    toggleCompleted(mainActivity, currentAssignments);
                    createSnackBarPopup(mainActivity, currentAssignments);
            }
        });
    }

    private void updateData(MainActivity activity) {
        titleView.setText(assignment.title);
        classView.setText(assignment.className);
        SimpleDateFormat dateFormat;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        if (prefs.getBoolean(SettingsActivity.timeEnabled, false)) {
            dateFormat = new SimpleDateFormat("h:mm a, EEE, MM/dd/yy", Locale.US);
        } else {
            dateFormat = new SimpleDateFormat("EEE, MM/dd/yy", Locale.US);
        }
        dateView.setText(dateFormat.format(assignment.dueDate.getTime()));
    }

    class BodyClickListener implements View.OnClickListener {
        Assignment assignment;
        FragmentManager f;
        MainActivity activity;

        BodyClickListener(Assignment assignment, MainActivity activity, FragmentManager fragmentManager) {
            this.assignment = assignment;
            this.activity = activity;
            f = fragmentManager;
        }

        @Override
        public void onClick(View v) {
            Bundle args = assignment.generateBundle();
//            args.putInt("sortIndex", sortIndex);
//            args.putBoolean("timeEnabled", activity.timeEnabled);
//            Log.v("AssignmentViewContainer", "timeEnabled=" + activity.timeEnabled);
//            Bundle settings = FileIO.readSettings(activity);
//            args.putAll(settings);


            DetailsDialog detailsDialog = new DetailsDialog();
            detailsDialog.setArguments(args);
            detailsDialog.show(f, "DetailsDialog");
        }
    }

    private void toggleCompleted(MainActivity mainActivity, ArrayList<Assignment> currentAssignments) {
        if (assignment.completed = !assignment.completed) {
            FileIO.inProgressAssignments.remove(assignment);
            FileIO.completedAssignments.add(assignment);
        } else {
            FileIO.completedAssignments.remove(assignment);
            FileIO.inProgressAssignments.add(assignment);
        }
        mainActivity.loadPanels(currentAssignments, sortIndex);
        FileIO.writeAssignmentsToFile(mainActivity);
    }

    /**
     * Makes for easy sorting by due date
     * @param o other object; should be an AssignmentViewWrapper
     * @return this.dueDate.compareTo(that.dueDate)
     */
    @Override
    public int compareTo(@NonNull Object o) {
        AssignmentViewWrapper box = (AssignmentViewWrapper) o;
        return assignment.dueDate.compareTo(box.assignment.dueDate);
    }

    /**
     * SnackBar pops up to make sure user is sure they want to mark assignment.
     * Disappears after a short length of time.
     * Recreates assignment if they choose to undo.
     *
     * @param activity reference to MainActivity instance
     */
    private void createSnackBarPopup(final MainActivity activity, final ArrayList<Assignment> currentAssignments) {
        String title, status;
        if (assignment.title.equals(""))
            title = "Untitled assignment";
        else
            title = "'" + assignment.title + "'";
        if (assignment.completed)
            status = "complete.";
        else
            status = "in progress.";
        Snackbar snackbar = Snackbar.make(
                activity.findViewById(R.id.coordinator),
                "Marked " + title + " as " + status,
                Snackbar.LENGTH_LONG
        );
        snackbar.setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCompleted(activity, currentAssignments);
            }
        });

        snackbar.show();
    }

}
