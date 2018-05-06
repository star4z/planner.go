package go.planner.plannergo;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static android.content.Context.ALARM_SERVICE;

/**
 * Lets user change fields of an Assignment
 * Created by Ben Phillips on 1/11/2018.
 */

public class EditDetailsDialog extends DialogFragment {

    EditText titleView, classView, dateView, timeView, descriptionView;
    Spinner typeView;
    Assignment oldAssignment;
    Calendar calendar;
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US);
    SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a".toLowerCase(), Locale.US);
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;

    int sortIndex;
    boolean timeEnabled;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater

        oldAssignment = new Assignment(getArguments());
        sortIndex = getArguments().getInt("sortIndex");
        timeEnabled = getArguments().getBoolean("timeEnabled");
        datePickerDialog = createDatePicker();
        timePickerDialog = createTimePickerDialog();

//        Log.v("EditDetailsDialog", "timeEnabled=" + timeEnabled);

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

                        MainActivity activity = (MainActivity) getActivity();

                        //Cancel old alarm, if it exists
                        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(ALARM_SERVICE);
                        PendingIntent oldPendingIntent = AlarmBroadcastReceiver.createPendingIntent(
                                oldAssignment, oldAssignment.hashCode(), timeEnabled, activity.getApplicationContext());
                        Log.v("EditDetailsDialog", "alarmManager=" + alarmManager);
                        assert alarmManager != null;
                        alarmManager.cancel(oldPendingIntent);

                        Toast.makeText(getActivity(), "Updated assignment.", Toast.LENGTH_SHORT).show();
                        //Redisplay notification, if it is displayed
//                        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(
//                                Context.NOTIFICATION_SERVICE);
//                        assert notificationManager != null;
//                        notificationManager.cancel(oldAssignment.hashCode());

                        //Create new alarm
                        NotificationAlarms.setNotificationTimer(
                                getActivity(), newAssignment, alarmManager,
                                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        );

                        FileIO.deleteAssignment(activity, oldAssignment);
                        activity.loadPanels((NewAssignment) oldAssignment, sortIndex);
                        FileIO.addAssignment(newAssignment);
                        FileIO.writeAssignmentsToFile(activity);
                        activity.loadPanels((NewAssignment) newAssignment, sortIndex);
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

    TimePickerDialog createTimePickerDialog() {
        return new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                timeView.setText(timeFormat.format(calendar.getTime()));
            }

        },
                oldAssignment.dueDate.get(Calendar.HOUR_OF_DAY),
                oldAssignment.dueDate.get(Calendar.MINUTE),
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

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.assignment_types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeView.setAdapter(adapter);

        //Initialize views with old values
        titleView.setText(oldAssignment.title);
        classView.setText(oldAssignment.className);
        dateView.setText(dateFormat.format(oldAssignment.dueDate.getTime()));
        descriptionView.setText(oldAssignment.description);
        typeView.setSelection(Assignment.spinnerPosition(oldAssignment.type));

        //display time if enabled
        if (timeEnabled) {
            //TODO: this changes the time to now since Assignment doesn't restore time, just date
            timeView.setText(timeFormat.format(oldAssignment.dueDate.getTime()));
            timeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    timePickerDialog.show();
                }
            });
        } else
            timeView.setVisibility(View.GONE);

        //prepare Calendar object with old value for manipulation by Date/Time Pickers
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
        args.putBoolean("timeEnabled", timeEnabled);

        DetailsDialog detailsDialog = new DetailsDialog();
        detailsDialog.setArguments(args);
        detailsDialog.show(f, "DetailsDialog");
    }//end openDetailsDialog()


}// end EditDetailsDialog class
