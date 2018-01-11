package go.planner.plannergo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Dialog with prompts to create a new assignment to add the main activity
 * Adds to inProgressAssignments by default
 * Created by bdphi on 1/11/2018.
 */

public class NewAssignmentDialog extends DialogFragment {

    EditText titleView, classView, dateView, descriptionView;
    Calendar calendar = Calendar.getInstance();
    Assignment assignment;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(
                R.layout.new_assignment_dialog,
                (ViewGroup) getActivity().findViewById(android.R.id.content),
                false
        );

        //Initialize EditTexts
        titleView = (EditText) view.findViewById(R.id.hw_title);
        classView = (EditText) view.findViewById(R.id.hw_class);
        dateView = (EditText) view.findViewById(R.id.hw_due_date);
        descriptionView = (EditText) view.findViewById(R.id.hw_description);

        //Build AlertDialog components
        builder.setView(view)
                .setTitle(R.string.new_a)
                // Add action buttons
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        //add new assignment to page
                        assignment = new Assignment(
                                titleView.getText().toString(),
                                classView.getText().toString(),
                                calendar,
                                descriptionView.getText().toString());
                        MainActivity activity = (MainActivity) getActivity();
                        activity.addAssignment(assignment);
                        activity.writeAssignmentsToFile();
                        activity.loadPanels();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        NewAssignmentDialog.this.getDialog().cancel();
                    }
                });

        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        calendar = Calendar.getInstance();

        dateView.setText(dateFormatter.format(calendar.getTime()));

        return builder.create();
    } // end onCreateDialog()


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}
