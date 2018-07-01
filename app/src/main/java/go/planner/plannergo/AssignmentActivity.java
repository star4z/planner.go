package go.planner.plannergo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

public abstract class AssignmentActivity extends AppCompatActivity {
    //Reads and writes involve this object
    NewAssignment assignment;
//    Calendar notifyDate = Calendar.getInstance();
//    Calendar notifyDate2 = Calendar.getInstance();

    //Settings file
    SharedPreferences prefs;

    //All the fucking views all up in one big list because Kotlin's nicer isn't it
    Toolbar toolbar;
    EditText hw_title;
    AutoCompleteTextView hw_class;
    EditText hw_due_date;
    EditText hw_due_time;
    EditText hw_description;
    Spinner hw_type;
    CheckBox is_priority;
    LinearLayout custom_notification;
    CheckBox enable_custom_notification;
    LinearLayout reminder_display;
    EditText n_days;
    EditText n_time;
    LinearLayout custom_notification_2;
    CheckBox extra_notification;
    LinearLayout extra_reminder_display;
    EditText r2_date;
    EditText r2_time;


    //Listeners
    TimePickerDialog dueTimePickerDialog;
    DatePickerDialog dueDatePickerDialog;
    TimePickerDialog notifyTimePickerDialog;
    TimePickerDialog notifyExtraTimePickerDialog;

    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US);
    SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a".toLowerCase(), Locale.US);

    /**
     * implementation should include super.onCreate(savedInstanceState;
     * and setContentView(R.layout.activity_assignment);
     *
     * @param savedInstanceState for restoring state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Intent thisIntent = getIntent();

        assignment = FileIO.getAssignment(thisIntent.getExtras() != null ? thisIntent.getExtras().getLong("uniqueID", -1L) : -1L);
        Log.v("AssignmentActivity", "assignment=" + assignment);

        connectViews();

        int layoutID = android.R.layout.simple_dropdown_item_1line;
        ArrayList<String> classArrayList = FileIO.classNames;
        String[] classes = classArrayList.toArray(new String[classArrayList.size()]);
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, layoutID, classes);
        hw_class.setAdapter(classAdapter);
        hw_class.setThreshold(0);



        ArrayList<String> typesArrayList = FileIO.types;
        String[] types = typesArrayList.toArray(new String[typesArrayList.size()]);
        ArrayAdapter<String> typesAdapter = new ArrayAdapter<>(this, layoutID, types);
//
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//                R.array.assignment_types_array, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hw_type.setAdapter(typesAdapter);

        manageVisibility();
        setUpListeners();
        initViews();
        initToolbar();
    }

    void initToolbar(){
        toolbar.setBackgroundColor(ColorPicker.getColorAssignment());
        toolbar.setTitleTextColor(ColorPicker.getColorAssignmentText());
        Drawable menuIcon;
        if (ColorPicker.getColorAssignmentText() == Color.BLACK) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
            menuIcon = ContextCompat.getDrawable(getApplicationContext(),
                    R.drawable.ic_more_vert_black_24dp);
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
            menuIcon = ContextCompat.getDrawable(getApplicationContext(),
                    R.drawable.ic_more_vert_white_24dp);
        }
        toolbar.setOverflowIcon(menuIcon);
        getWindow().setStatusBarColor(ColorPicker.getColorAssignmentAccent());
        setSupportActionBar(toolbar);
    }


    /**
     * Determines which views should be visible when the activity starts.
     */
    abstract void manageVisibility();

    void connectViews() {
        toolbar = findViewById(R.id.toolbar);
        hw_title = findViewById(R.id.hw_title);
        hw_class = findViewById(R.id.hw_class);
        hw_due_date = findViewById(R.id.hw_due_date);
        hw_due_time = findViewById(R.id.hw_due_time);
        hw_description = findViewById(R.id.hw_description);
        hw_type = findViewById(R.id.hw_type);
        is_priority = findViewById(R.id.is_priority);
        custom_notification = findViewById(R.id.custom_notification);
        enable_custom_notification = findViewById(R.id.enable_custom_notification);
        reminder_display = findViewById(R.id.reminder_display);
        n_days = findViewById(R.id.n_days);
        n_time = findViewById(R.id.n_time);
        custom_notification_2 = findViewById(R.id.custom_notification_2);
        extra_notification = findViewById(R.id.extra_notification);
        extra_reminder_display = findViewById(R.id.extra_reminder_display);
        r2_date = findViewById(R.id.r2_date);
        r2_time = findViewById(R.id.r2_time);
    }

    abstract void initViews();

    abstract void setUpListeners();

    public static void setToolbarMenuItemTextColor(final Toolbar toolbar, final @ColorRes int color, @IdRes final int resId) {
        if (toolbar != null) {
            for (int i = 0; i < toolbar.getChildCount(); i++) {
                final View view = toolbar.getChildAt(i);
                if (view instanceof ActionMenuView) {
                    final ActionMenuView actionMenuView = (ActionMenuView) view;
                    // view children are accessible only after layout-ing
                    actionMenuView.post(new Runnable() {
                        @Override
                        public void run() {
                            for (int j = 0; j < actionMenuView.getChildCount(); j++) {
                                final View innerView = actionMenuView.getChildAt(j);
                                if (innerView instanceof ActionMenuItemView) {
                                    final ActionMenuItemView itemView = (ActionMenuItemView) innerView;
                                    if (resId == itemView.getId()) {
                                        itemView.setTextColor(ContextCompat.getColor(toolbar.getContext(), color));
                                    }
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_assignment_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_assignment:
                Log.v("NewAssignmentActivity", "save button pressed");
                saveAssignment();
            case android.R.id.home:
                navigateUpTo(new Intent(this, MainActivity.class));
                return true;
                default:
                    return super.onOptionsItemSelected(item);


        }
    }

    /**
     * creates dialog to modify a date field
     *
     * @param dateToModify 0 for dueDate, 1 for notificationDate1, 2 for NotificationDate2
     * @param view         field to modify
     * @return TimePickerDialog
     */
    TimePickerDialog createTimePicker(final int dateToModify, final EditText view) {
        final Calendar calendar;
        switch (dateToModify) {
            default:
            case 0:
                calendar = (assignment.dueDate == null) ?
                        Calendar.getInstance() : assignment.dueDate;
                break;
            case 1:
                calendar = (assignment.notificationDate1 == null) ?
                        Calendar.getInstance() : assignment.notificationDate1;
                break;
            case 2:
                calendar = (assignment.notificationDate2 == null) ?
                        Calendar.getInstance() : assignment.notificationDate2;
                break;
        }

        return new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                calendar.set(HOUR_OF_DAY, hourOfDay);
                calendar.set(MINUTE, minute);
                view.setText(timeFormat.format(calendar.getTime()));
                switch (dateToModify) {
                    default:
                    case 0:
                        assignment.dueDate = calendar;
                        break;
                    case 1:
                        assignment.notificationDate1 = calendar;
                        break;
                    case 2:
                        assignment.notificationDate2 = calendar;
                }
            }
        }, calendar.get(HOUR_OF_DAY), calendar.get(MINUTE), false);

    }

    /**
     * reads and writes to date
     * outputs changes to view
     */
    DatePickerDialog createDatePicker(final Calendar date, final EditText view) {
        return new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                date.set(YEAR, year);
                date.set(MONTH, month);
                date.set(DAY_OF_MONTH, dayOfMonth);
                view.setText(dateFormat.format(date.getTime()));
            }
        }, date.get(YEAR), date.get(MONTH), date.get(DAY_OF_MONTH));
    }

    abstract public void toggleCustomNotification(View view);

    abstract public void toggleExtraNotification(View view);

    public void togglePriorityAssignment(View view) {
        if (assignment.priority == 1) {
            assignment.priority = 0;
            is_priority.setChecked(false);
        } else {
            assignment.priority = 1;
            is_priority.setChecked(true);
        }
    }


    abstract void saveAssignment();

}
