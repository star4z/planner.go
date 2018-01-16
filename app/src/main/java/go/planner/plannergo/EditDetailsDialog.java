package go.planner.plannergo;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
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
 * Lets user change fields of an Assignment
 * Created by bdphi on 1/11/2018.
 */

public class EditDetailsDialog extends DialogFragment {
    EditText titleView, classView, dateView, descriptionView;
    //    Calendar calendar = Calendar.getInstance();
    Assignment oldAssignment;
    Calendar calendar;
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    DatePickerDialog datePickerDialog;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        oldAssignment = Assignment.getAssignment(getArguments());
        datePickerDialog = createDatePicker();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = initializeViews();

        //Build AlertDialog components
        builder.setView(view)
                .setTitle(R.string.edit)
                // Add action buttons
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //add new assignment to page
                        Assignment newAssignment = new Assignment(
                                titleView.getText().toString(),
                                classView.getText().toString(),
                                calendar,
                                descriptionView.getText().toString(),
                                oldAssignment.completed
                        );
//                            ((MainActivity) getActivity()).editAssignment(assignmentView, oldAssignment);
                        MainActivity activity = (MainActivity) getActivity();
                        activity.deleteAssignment(oldAssignment);
                        activity.addAssignment(newAssignment);
                        activity.writeAssignmentsToFile();
                        activity.loadPanels();
                        openDetailsDialog(newAssignment, getFragmentManager());
                        EditDetailsDialog.this.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        openDetailsDialog(oldAssignment, getFragmentManager());
                        EditDetailsDialog.this.getDialog().cancel();
                    }
                });

        return builder.create();
    } // end onCreateDialog()

    DatePickerDialog createDatePicker() {
        return new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(year, monthOfYear, dayOfMonth);
                dateView.setText(dateFormat.format(calendar.getTime()));
            }

        },
                oldAssignment.dueDate.get(Calendar.YEAR),
                oldAssignment.dueDate.get(Calendar.MONTH),
                oldAssignment.dueDate.get(Calendar.DATE));

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

        titleView.setText(oldAssignment.title);
        classView.setText(oldAssignment.className);
        dateView.setText(dateFormat.format(oldAssignment.dueDate.getTime()));
        descriptionView.setText(oldAssignment.description);

        calendar = (Calendar) oldAssignment.dueDate.clone();

        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        return view;
    }

    void openDetailsDialog(Assignment assignment, FragmentManager f) {
        Bundle args = Assignment.generateBundle(assignment);

        DetailsDialog detailsDialog = new DetailsDialog();
        detailsDialog.setArguments(args);
        detailsDialog.show(f, "DetailsDialog");
    }//end openDetailsDialog()


}// end EditDetailsDialog class
