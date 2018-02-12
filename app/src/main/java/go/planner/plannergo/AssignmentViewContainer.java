package go.planner.plannergo;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
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

class AssignmentViewContainer implements Comparable<Object> {

    LinearLayout container;

    private TextView titleView, classView, dateView;
    private int sortIndex;

    private final Assignment assignment;

    AssignmentViewContainer(final Activity activity, Assignment newAssignment, int sortIndex) {
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

        updateData();

        //click functionality (opens DetailsDialog)
        body.setOnClickListener(new BodyClickListener(newAssignment, f));
        //checkBox functionality
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    final MainActivity mainActivity = (MainActivity) activity;
                    ArrayList<Assignment> currentAssignments;
                    if (assignment.completed)
                        currentAssignments = mainActivity.completedAssignments;
                    else
                        currentAssignments = mainActivity.inProgressAssignments;
                    toggleCompleted(mainActivity, currentAssignments);
                    createSnackBarPopup(mainActivity, activity.getFragmentManager(), currentAssignments);
                }
            }
        });
    }

    private void updateData() {
        titleView.setText(assignment.title);
        classView.setText(assignment.className);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MM/dd/yy", Locale.US);
        dateView.setText(dateFormat.format(assignment.dueDate.getTime()));
    }

    class BodyClickListener implements View.OnClickListener {
        Assignment assignment;
        FragmentManager f;

        BodyClickListener(Assignment assignment, FragmentManager fragmentManager) {
            this.assignment = assignment;
            f = fragmentManager;
        }

        @Override
        public void onClick(View v) {
            Bundle args = assignment.generateBundle();
            args.putInt("sortIndex", sortIndex);

            DetailsDialog detailsDialog = new DetailsDialog();
            detailsDialog.setArguments(args);
            detailsDialog.show(f, "DetailsDialog");
        }
    }

    private void toggleCompleted(MainActivity mainActivity, ArrayList<Assignment> currentAssignments) {
        if (assignment.completed = !assignment.completed) {
            mainActivity.inProgressAssignments.remove(assignment);
            mainActivity.completedAssignments.add(assignment);
        } else {
            mainActivity.completedAssignments.remove(assignment);
            mainActivity.inProgressAssignments.add(assignment);
        }
        mainActivity.loadPanels(currentAssignments, sortIndex);
        mainActivity.writeAssignmentsToFile();
    }

    @Override
    public int compareTo(@NonNull Object o) {
        AssignmentViewContainer box = (AssignmentViewContainer) o;
        return assignment.dueDate.compareTo(box.assignment.dueDate);
    }

    /**
     * SnackBar pops up to make sure user is sure they want to mark assignment.
     * Disappears after a short length of time.
     * Recreates assignment if they choose to undo.
     *
     * @param activity reference to MainActivity instance
     * @param manager  reference to current FragmentManager
     */
    private void createSnackBarPopup(final MainActivity activity, final FragmentManager manager, final ArrayList<Assignment> currentAssignments) {
        String title, status;
        if (assignment.title.equals(""))
            title = "untitled assignment";
        else
            title = "'" + assignment.title + "'";
        if (assignment.completed)
            status = "complete.";
        else
            status = "in progress.";
        Snackbar snackbar = Snackbar.make(
                activity.findViewById(R.id.coordinator),
                "Marked " + title + " as " + status,
                Snackbar.LENGTH_SHORT
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
