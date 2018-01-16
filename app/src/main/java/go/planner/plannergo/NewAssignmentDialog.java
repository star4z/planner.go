package go.planner.plannergo;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Dialog with prompts to create a new oldAssignment to add the main activity
 * Adds to inProgressAssignments by default
 * Created by bdphi on 1/11/2018.
 */

public class NewAssignmentDialog extends DialogFragment {

    EditText titleView, classView, dateView, descriptionView;
    Assignment assignment;
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    DatePickerDialog datePickerDialog;

    /**
     * Handles the creation of the Dialog
     *
     * @param savedInstanceState used to restore instance state; bundle contains data to recreate
     *                           oldAssignment
     * @return the Dialog to be displayed
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = initializeViews();

        assignment = getAssignment();

        datePickerDialog = createDatePicker();

        //Build AlertDialog components
        builder.setView(view)
                .setTitle(R.string.new_a)
                // Add action buttons
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        //add new oldAssignment to page
                        assignment = new Assignment(
                                titleView.getText().toString(),
                                classView.getText().toString(),
                                assignment.dueDate,
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

        dateView.setText(dateFormat.format(assignment.dueDate.getTime()));

        return builder.create();
    } // end onCreateDialog()


    DatePickerDialog createDatePicker() {
        return new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                assignment.dueDate.set(year, monthOfYear, dayOfMonth);
                dateView.setText(dateFormat.format(assignment.dueDate.getTime()));
            }
        },
                assignment.dueDate.get(Calendar.YEAR),
                assignment.dueDate.get(Calendar.MONTH),
                assignment.dueDate.get(Calendar.DATE)
        );
    }

    View initializeViews() {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(
                R.layout.new_assignment_dialog,
                (ViewGroup) getActivity().findViewById(android.R.id.content), false);

        titleView = (EditText) view.findViewById(R.id.hw_title);
        classView = (EditText) view.findViewById(R.id.hw_class);
        dateView = (EditText) view.findViewById(R.id.hw_due_date);
        descriptionView = (EditText) view.findViewById(R.id.hw_description);

        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        return view;
    }

    Assignment getAssignment() {
        return new Assignment(
                titleView.getText().toString(),
                classView.getText().toString(),
                Calendar.getInstance(),
                descriptionView.getText().toString()
        );
    }
}
