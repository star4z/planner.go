package go.planner.plannergo;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Stores assignment GUI info
 * Handles checkbox and body OnClickListeners
 * <p>
 * Created by Ben Phillips on 12/22/2017.
 */

class AssignmentCheckBox implements Comparable<Object> {

    LinearLayout container;

    private TextView titleView, classView, dateView;

    private final Assignment assignment;

    AssignmentCheckBox(final Activity activity, Assignment newAssignment, FragmentManager f) {
        assignment = newAssignment;

        LayoutInflater inflater = activity.getLayoutInflater();
        ViewGroup parent = (ViewGroup) activity.findViewById(android.R.id.content);
        container = (LinearLayout) inflater.inflate(R.layout.assignment_layout, parent, false);

        RelativeLayout body = (RelativeLayout) container.findViewById(R.id.body);
        CheckBox checkBox = (CheckBox) container.findViewById(R.id.checkbox);
        titleView = (TextView) container.findViewById(R.id.title);
        classView = (TextView) container.findViewById(R.id.class_name);
        dateView = (TextView) container.findViewById(R.id.date);

        updateData();

        //click functionality (opens DetailsDialog)
        body.setOnClickListener(new BodyClickListener(newAssignment, f));
        //checkBox functionality
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MainActivity mainActivity = (MainActivity) activity;
                    if (!assignment.completed) {
                        assignment.completed = true;
                        System.out.println(mainActivity.inProgressAssignments.remove(assignment));
                        mainActivity.completedAssignments.add(assignment);
                    } else {
                        assignment.completed = false;
                        System.out.println(mainActivity.completedAssignments.remove(assignment));
                        mainActivity.inProgressAssignments.add(assignment);
                    }
                    mainActivity.writeAssignmentsToFile();
                    mainActivity.loadPanels();
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
            Bundle args = new Bundle();

            args.putString("title", assignment.title);
            args.putString("class", assignment.className);
            args.putInt("year", assignment.dueDate.get(Calendar.YEAR));
            args.putInt("month", assignment.dueDate.get(Calendar.MONTH));
            args.putInt("dateView", assignment.dueDate.get(Calendar.DATE));
            args.putString("description", assignment.description);
            args.putBoolean("completed", assignment.completed);

            DetailsDialog detailsDialog = new DetailsDialog();
            detailsDialog.setArguments(args);
            detailsDialog.show(f, "DetailsDialog");
        }
    }

    @Override
    public int compareTo(@NonNull Object o) {
        AssignmentCheckBox box = (AssignmentCheckBox) o;
        return assignment.dueDate.compareTo(box.assignment.dueDate);
    }

}
