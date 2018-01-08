package go.planner.plannergo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Stores assignment GUI info
 * Not quite sure how yet.
 * <p>
 * Created by bdphi on 12/22/2017.
 */

class AssignmentCheckBox implements Comparable {

    LinearLayout container;

    RelativeLayout body;
    CheckBox checkBox;

    TextView titleView, classView, dateView;

    Assignment assignment;

    AssignmentCheckBox(Activity activity, Assignment assignment) {
        LayoutInflater inflater = activity.getLayoutInflater();
        container = (LinearLayout) inflater.inflate(R.layout.assignment_layout, null);

        body = (RelativeLayout) container.findViewById(R.id.body);
        checkBox = (CheckBox) container.findViewById(R.id.checkbox);
        titleView = (TextView) container.findViewById(R.id.title);
        classView = (TextView) container.findViewById(R.id.class_name);
        dateView = (TextView) container.findViewById(R.id.date);

        updateData(assignment);

    }

    void updateData(Assignment assignment){
        this.assignment = assignment;

        titleView.setText(assignment.title);
        classView.setText(assignment.className);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MM/dd/yy", Locale.US);
        dateView.setText(dateFormat.format(assignment.dueDate.getTime()));
    }

    @Override
    public int compareTo(@NonNull Object o) {
        AssignmentCheckBox box = (AssignmentCheckBox) o;
        return assignment.dueDate.compareTo(box.assignment.dueDate);
    }

    public static class AssignmentDetailsDialog extends DialogFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = getActivity().getLayoutInflater();

            View view = inflater.inflate(R.layout.assignment_details_dialog, null);

            TextView title = (TextView) view.findViewById(R.id.title);
            TextView className = (TextView) view.findViewById(R.id.class_name);
            TextView date = (TextView) view.findViewById(R.id.date);
            TextView description = (TextView) view.findViewById(R.id.description);


            builder.setView(view);

        }

        public void closeDetails(View view){

        }
    }

}
