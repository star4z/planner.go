package go.planner.plannergo;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static android.content.Context.ALARM_SERVICE;

/**
 * Dialog with prompts to create a new oldAssignment to add the main activity
 * Adds to inProgressAssignments by default
 * Created by bdphi on 1/11/2018.
 */

public class NewAssignmentDialog extends DialogFragment {
    EditText titleView, classView, dateView, timeView, descriptionView;
    Spinner typeView;
    Assignment assignment;
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US);
    SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a".toLowerCase(), Locale.US);
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    boolean timeEnabled;

    /**
     * Handles the creation of the Dialog
     *
     * @param savedInstanceState used to restore instance state
     * @return the Dialog to be displayed
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        timeEnabled = getArguments().getBoolean("timeEnabled");
        Log.v("NewAssignmentDialog", "timeEnabled=" + timeEnabled);

        View view = initializeViews();

        assignment = new Assignment();
        assignment.dueDate.set(Calendar.HOUR_OF_DAY, 8);
        assignment.dueDate.set(Calendar.MINUTE, 0);
        datePickerDialog = createDatePicker();
        timePickerDialog = createTimePickerDialog();

        //Build AlertDialog components
        builder.setView(view)
                .setTitle(R.string.new_a)
                // Add action buttons
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //add newAssignment to page
                        assignment = new Assignment(
                                titleView.getText().toString(),
                                classView.getText().toString(),
                                assignment.dueDate,
                                descriptionView.getText().toString(),
                                false,
                                (String) typeView.getSelectedItem());
                        Log.v("NewAD", "assignment=" + assignment);
                        MainActivity activity = (MainActivity) getActivity();
                        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(ALARM_SERVICE);
                        activity.setNotificationTimer(assignment, alarmManager);
                        activity.addAssignment(assignment);
                        activity.writeAssignmentsToFile();
                        activity.loadPanels(assignment, getArguments().getInt("sortIndex"));
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        NewAssignmentDialog.this.getDialog().cancel();
                    }
                });

        dateView.setText(dateFormat.format(assignment.dueDate.getTime()));
        if (timeEnabled)
            timeView.setText(timeFormat.format(assignment.dueDate.getTime()));
        else
            timeView.setWidth(0);

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

    TimePickerDialog createTimePickerDialog() {
        return new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                assignment.dueDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                assignment.dueDate.set(Calendar.MINUTE, minute);
                timeView.setText(timeFormat.format(assignment.dueDate.getTime()));
            }

        },
                assignment.dueDate.get(Calendar.HOUR_OF_DAY),
                assignment.dueDate.get(Calendar.MINUTE),
                false);
    }

    View initializeViews() {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(
                R.layout.dialog_new_assignment,
                (ViewGroup) getActivity().findViewById(android.R.id.content), false);

        titleView = view.findViewById(R.id.hw_title);
        classView = view.findViewById(R.id.hw_class);
        dateView = view.findViewById(R.id.hw_due_date);
        timeView = view.findViewById(R.id.hw_due_time);
        descriptionView = view.findViewById(R.id.hw_description);
        typeView = view.findViewById(R.id.hw_type);

        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        if (timeEnabled)
            timeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    timePickerDialog.show();
                }
            });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.assignment_types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeView.setAdapter(adapter);

        return view;
    }

}
