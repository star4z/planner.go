package go.planner.plannergo;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Locale;

/**
 * Enables user to modify settings for app
 * Created by bdphi on 1/30/2018.
 */

public class SettingsActivity extends AppCompatActivity {

    Bundle settings;

    //    int defaultSortIndex = 0;
//    boolean overdueFirst = true;
    final String sortOptions[] = new String[]{"Sort by date", "Sort by class", "Sort by title", "Sort by type"};
//    boolean timeEnabled = false;
//    Calendar notificationDate;

    TimePickerDialog timePickerDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settings = FileIO.readSettings(this);
        updateViews();
        timePickerDialog = createTimePickerDialog();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        getWindow().setStatusBarColor(getResources().getColor(R.color.p1_secondary_dark));
        setSupportActionBar(toolbar);

    }

    /**
     * Handles any option menu items; probably will only handle up functionality
     * Handles storing data in file that was manipulated.
     *
     * @param item selected item; will always be home unless menu is defined
     * @return true if action performed; else calls super
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Returns to the Main activity
            case android.R.id.home:
                save();
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void save() {
        settings.putBoolean("overdueFirst", !((Switch) findViewById(R.id.overdue_switch)).isChecked());
        settings.putBoolean("timeEnabled", ((Switch) findViewById(R.id.enable_time_switch)).isChecked());
        FileIO.writeSettings(this, settings);
    }

    @Override
    public void finish() {
        save();
        super.finish();
    }


    /**
     * Opens a dialog with a series of titles, descriptions, with radio button to indicate last
     * checked item.
     * For use with items that have more than two possible states.
     * Method should be called from xml
     *
     * @param view View that received click
     */
    public void openMenuDialog(View view) {
        String[] options;
        String[] descriptions;
        String tag = "";

        SettingsSelectionDialog dialog = new SettingsSelectionDialog();
        Bundle bundle = (Bundle) settings.clone();

        //TODO: add option to enable notifications (necessary?)

        switch (view.getId()) {
            case (R.id.setting_sort_type):
//                Log.v("SettingsActivity","settings.defaultSortIndex=" + );
                options = sortOptions;
                descriptions = new String[]{
                        "Assignments are grouped by date; \nEarliest assignment is listed first.",
                        "Assignments are grouped by class. \nClasses are listed alphabetically.",
                        "Assignments are listed alphabetically by title.",
                        "Assignments are grouped by type."};

                bundle.putStringArray("options", options);
                bundle.putStringArray("descriptions", descriptions);
                bundle.putString("title", "Default Sort");

                tag = "Settings: Sort Type";
                break;
        }

        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), tag);
    }

    public void overdueSwitchToggle(View view) {
        Switch aSwitch = findViewById(R.id.overdue_switch);
        aSwitch.toggle();
    }

    TimePickerDialog createTimePickerDialog() {

        return new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                Log.v("SettingsActivity", "timePicker time: " + hourOfDay + ":" + minute);
                settings.putInt("alarmHour", hourOfDay);
                settings.putInt("alarmMinute", minute);
                updateViews();
            }

        },
                settings.getInt("alarmHour"),
                settings.getInt("alarmMinute"),
                false);
    }

    public void showDatePickerDialog(View view) {
        timePickerDialog.show();
    }

    public void enableTimeToggle(View view) {
        Switch aSwitch = findViewById(R.id.enable_time_switch);
        aSwitch.toggle();
    }

    /**
     * For use in initializing the Activity and returning the Activity to focus after a dialog
     * has been opened.
     */
    public void updateViews() {
        FileIO.writeSettings(this, settings);

        TextView defaultSortSelection = findViewById(R.id.selected_sort_type);
        defaultSortSelection.setText(sortOptions[settings.getInt("defaultSortIndex")]);

        Switch overdueSwitch = findViewById(R.id.overdue_switch);
        overdueSwitch.setChecked(!settings.getBoolean("overdueFirst"));

        Switch enableTimeSwitch = findViewById(R.id.enable_time_switch);
        enableTimeSwitch.setChecked(settings.getBoolean("timeEnabled"));

        TextView currentNotifTime = findViewById(R.id.notification_time_current);

        int hour = settings.getInt("alarmHour");
        String minute = String.format(Locale.US, "%02d", settings.getInt("alarmMinute"));

        String notifTime;

        String ampm = (hour / 12 == 1)? "pm":"am";
        hour = hour % 12 == 0? 12 : hour % 12;

        notifTime = hour + ":" + minute + ampm;

        currentNotifTime.setText(notifTime);
    }

    public void about(View view) {
        startActivity(new Intent(this, AboutActivity.class));
    }

}
