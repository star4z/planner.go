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

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Displays all information about an Assignment and gives options to edit & delete it
 * Created by Ben Phillips on 1/11/2018.
 */

public class DetailsDialog extends DialogFragment {
    //Assignment oldAssignment;
    TextView textView, classNameView, dateView, descriptionView;
    Assignment assignment;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        assignment = Assignment.getAssignment(getArguments());

        View view = initializeViews();

        //editButton functionality
        ImageView editButton = (ImageView) view.findViewById(R.id.edit);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditDetailsDialog editDetailsDialog = new EditDetailsDialog();
                editDetailsDialog.setArguments(Assignment.generateBundle(assignment));
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

    View initializeViews() {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(
                R.layout.assignment_details_dialog,
                (ViewGroup) getActivity().findViewById(android.R.id.content), false);

        textView = (TextView) view.findViewById(R.id.title);
        classNameView = (TextView) view.findViewById(R.id.class_name);
        dateView = (TextView) view.findViewById(R.id.date);
        descriptionView = (TextView) view.findViewById(R.id.description);

        return view;
    }


    public void updateViews() {
        textView.setText(assignment.title);
        classNameView.setText(assignment.className);
        dateView.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.US)
                .format(assignment.dueDate.getTime()));
        descriptionView.setText(assignment.description);
    }
}
