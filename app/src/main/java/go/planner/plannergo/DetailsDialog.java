package go.planner.plannergo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Displays all information about an Assignment and gives options to edit & delete it
 * Created by Ben Phillips on 1/11/2018.
 */

public class DetailsDialog extends DialogFragment {
    //Assignment assignment;
    TextView textView, classNameView, dateView, descriptionView;
    Assignment assignment;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

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

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(
                R.layout.assignment_details_dialog,
                (ViewGroup) getActivity().findViewById(android.R.id.content), false);

        textView = (TextView) view.findViewById(R.id.title);
        classNameView = (TextView) view.findViewById(R.id.class_name);
        dateView = (TextView) view.findViewById(R.id.date);
        descriptionView = (TextView) view.findViewById(R.id.description);

        //editButton functionality
        ImageView editButton = (ImageView) view.findViewById(R.id.edit);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();

                args.putString("title", title);
                args.putString("class", className);
                args.putInt("year", year);
                args.putInt("month", month);
                args.putInt("dateView", date);
                args.putString("description", description);
                args.putBoolean("completed", completed);

                EditDetailsDialog editDetailsDialog = new EditDetailsDialog();
                editDetailsDialog.setArguments(args);
                editDetailsDialog.show(getFragmentManager(), "DetailsDialog");
                dismiss();
            }
        });

        //deleteButton functionality
        ImageView deleteButton = (ImageView) view.findViewById(R.id.delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).deleteAssignment(assignment);
                dismiss();
            }
        });

        ImageView closeButton = (ImageView) view.findViewById(R.id.close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        updateViews();

        builder.setView(view);

        return builder.create();

    }


    public void updateViews() {
//            Assignment assignment = ((MainActivity) getActivity()).getAssignment();
        textView.setText(assignment.title);
        classNameView.setText(assignment.className);
        dateView.setText(
                ((MainActivity) getActivity()).dateFormatter.format(assignment.dueDate.getTime())
        );
        descriptionView.setText(assignment.description);
    }
}
