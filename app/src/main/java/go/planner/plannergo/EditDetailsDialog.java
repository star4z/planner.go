package go.planner.plannergo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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
    Calendar calendar = Calendar.getInstance();
    Assignment assignment;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final String title = getArguments().getString("title");
        final String className = getArguments().getString("class");
        Calendar dueDate = Calendar.getInstance();
        final int year = getArguments().getInt("year");
        final int month = getArguments().getInt("month");
        final int date = getArguments().getInt("dateView");
        dueDate.set(year, month, date);
        final String description = getArguments().getString("description");
        final boolean completed = getArguments().getBoolean("completed");

        assignment = new Assignment(title, className, dueDate, description, completed);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(R.layout.new_assignment_dialog, null);

        //Initialize EditTexts
        titleView = (EditText) view.findViewById(R.id.hw_title);
        titleView.setText(title);
        classView = (EditText) view.findViewById(R.id.hw_class);
        classView.setText(className);
        dateView = (EditText) view.findViewById(R.id.hw_due_date);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        dateView.setText(dateFormatter.format(dueDate.getTime()));
        descriptionView = (EditText) view.findViewById(R.id.hw_description);
        descriptionView.setText(description);

        //Build AlertDialog components
        builder.setView(view)
                .setTitle(R.string.edit)
                // Add action buttons
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        calendar.set(year, month, date);
                        //add new assignment to page
                        Assignment newAssignment = new Assignment(
                                titleView.getText().toString(),
                                classView.getText().toString(),
                                calendar,
                                descriptionView.getText().toString(),
                                completed
                        );
//                            ((MainActivity) getActivity()).editAssignment(assignmentView, assignment);
                        MainActivity activity = (MainActivity) getActivity();
                        activity.deleteAssignment(assignment);
                        activity.addAssignment(newAssignment);
                        activity.writeAssignmentsToFile();
                        activity.loadPanels();
                        openDetailsDialog(newAssignment, getFragmentManager());
                        EditDetailsDialog.this.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        openDetailsDialog(assignment, getFragmentManager());
                        EditDetailsDialog.this.getDialog().cancel();
                    }
                });

        return builder.create();
    } // end onCreateDialog()

    void openDetailsDialog(Assignment assignment, FragmentManager f) {
        Bundle args = new Bundle();

        args.putString("title", assignment.title);
        args.putString("class", assignment.className);
        args.putInt("year", assignment.dueDate.get(Calendar.YEAR));
        args.putInt("month", assignment.dueDate.get(Calendar.MONTH));
        args.putInt("dateView", assignment.dueDate.get(Calendar.DATE));
        args.putString("description", assignment.description);

        DetailsDialog detailsDialog = new DetailsDialog();
        detailsDialog.setArguments(args);
        detailsDialog.show(f, "DetailsDialog");
    }//end openDetailsDialog()
}// end EditDetailsDialog class
