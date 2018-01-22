package go.planner.plannergo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
    TextView textView, classNameView, dateView, descriptionView, typeView;
    Assignment assignment;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        assignment = new Assignment(getArguments());

        View view = initializeViews();

        //editButton functionality
        ImageView editButton = (ImageView) view.findViewById(R.id.edit);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditDetailsDialog editDetailsDialog = new EditDetailsDialog();
                editDetailsDialog.setArguments(assignment.generateBundle());
                editDetailsDialog.show(getFragmentManager(), "DetailsDialog");
                dismiss();
            }
        });

        //deleteButton functionality
        ImageView deleteButton = (ImageView) view.findViewById(R.id.delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MainActivity activity = (MainActivity) getActivity();
                final FragmentManager manager = getFragmentManager();

                activity.deleteAssignment(assignment);

                createSnackBarPopup(activity, manager);

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
                R.layout.dialog_details,
                (ViewGroup) getActivity().findViewById(android.R.id.content), false);

        textView = (TextView) view.findViewById(R.id.title);
        classNameView = (TextView) view.findViewById(R.id.class_name);
        dateView = (TextView) view.findViewById(R.id.date);
        descriptionView = (TextView) view.findViewById(R.id.description);
        typeView = (TextView) view.findViewById(R.id.type);

        return view;
    }

    void updateViews() {
        textView.setText(assignment.title);
        classNameView.setText(assignment.className);
        dateView.setText(new SimpleDateFormat("EEE MM.dd.yyyy", Locale.US)
                .format(assignment.dueDate.getTime()));
        descriptionView.setText(assignment.description);
        typeView.setText(assignment.type);
    }

    /**
     * SnackBar pops up to make sure user is sure they want to delete assignment.
     * Disappears after a short length of time.
     * Recreates assignment if they choose to undo.
     *
     * @param activity reference to MainActivity instance
     * @param manager reference to current FragmentManager
     */
    void createSnackBarPopup(final MainActivity activity, final FragmentManager manager) {
        String title;
        if (assignment.title.equals(""))
            title = "untitled assignment";
        else
            title = "'" + assignment.title + "'";
        Snackbar waitDontDeleteMeYet = Snackbar.make(
                activity.findViewById(R.id.coordinator),
                title+ " was deleted.",
                Snackbar.LENGTH_SHORT
        );
        waitDontDeleteMeYet.setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.addAssignment(assignment);
                activity.loadPanels();
                DetailsDialog detailsDialog = new DetailsDialog();
                detailsDialog.setArguments(getArguments());
                detailsDialog.show(manager, "DetailsDialog");
            }
        });

        waitDontDeleteMeYet.show();
    }
}
