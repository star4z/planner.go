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
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Lets user change fields of an Assignment
 * Created by Ben Phillips on 1/11/2018.
 */

public class EditDetailsDialog extends DialogFragment {

    EditText titleView, classView, dateView, descriptionView;
    Spinner typeView;
    Assignment oldAssignment;
    Calendar calendar;
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    DatePickerDialog datePickerDialog;
    int sortIndex;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater

        oldAssignment = new Assignment(getArguments());
       sortIndex = getArguments().getInt("sortIndex");
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
//                        oldAssignment.type = Assignment.getHomeworkType(typeView);
                        //add new assignment to page
                        Assignment newAssignment = new Assignment(
                                titleView.getText().toString(),
                                classView.getText().toString(),
                                calendar,
                                descriptionView.getText().toString(),
                                oldAssignment.completed,
                                typeView.getSelectedItem().toString()
                        );
//                            ((MainActivity) getActivity()).editAssignment(assignmentView, oldAssignment);
                        MainActivity activity = (MainActivity) getActivity();
                        activity.deleteAssignment(oldAssignment, sortIndex);
                        activity.addAssignment(newAssignment);
                        activity.writeAssignmentsToFile();
                        activity.loadPanels(newAssignment, sortIndex);
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
                R.layout.dialog_new_assignment,
                (ViewGroup) getActivity().findViewById(android.R.id.content), false);

        titleView = view.findViewById(R.id.hw_title);
        classView = view.findViewById(R.id.hw_class);
        dateView = view.findViewById(R.id.hw_due_date);
        descriptionView = view.findViewById(R.id.hw_description);
        typeView = view.findViewById(R.id.hw_type);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.assignment_types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeView.setAdapter(adapter);

        titleView.setText(oldAssignment.title);
        classView.setText(oldAssignment.className);
        dateView.setText(dateFormat.format(oldAssignment.dueDate.getTime()));
        descriptionView.setText(oldAssignment.description);
        typeView.setSelection(Assignment.spinnerPosition(oldAssignment.type));

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
        Bundle args = assignment.generateBundle();
        args.putInt("sortIndex", sortIndex);

        DetailsDialog detailsDialog = new DetailsDialog();
        detailsDialog.setArguments(args);
        detailsDialog.show(f, "DetailsDialog");
    }//end openDetailsDialog()


}// end EditDetailsDialog class
