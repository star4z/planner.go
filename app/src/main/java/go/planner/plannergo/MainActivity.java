package go.planner.plannergo;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String ASSIGNMENTS_FILE_NAME = "assignmentsFile";
    final String SETTINGS_FILE_NAME = "planner.settings";
    public static final String MARK_DONE = "app.planner.MARK_DONE";

    //Settings data
    public int defaultSortIndex = 0;
    int currentSortIndex = 0;
    public boolean overdueFirst = true;
    public int alarmHourOfDay = 8;
    public int alarmMinuteOfDay = 0;
    public int daysBeforeDueDate = 1;
    public boolean timeEnabled = false;
    Calendar notificationTime;

    //Assignment storage
    ArrayList<Assignment> inProgressAssignments = new ArrayList<>();
    ArrayList<Assignment> completedAssignments = new ArrayList<>();
    //Stores all views that need to be manipulated for removal when appropriate; unnecessary?
    ArrayList<View> currentViews = new ArrayList<>();
    //Quick references
    LinearLayout parent;
    Toolbar myToolbar;
    private DrawerLayout mDrawerLayout;

    //TODO: add tutorial

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parent = findViewById(R.id.body);

        setUpNavDrawer();

        checkFirstRun();

        readSettings();
        currentSortIndex = defaultSortIndex;

        myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        readAssignmentsFromFile();
    }

    //GUI setup methods
    private void setUpNavDrawer() {
        String[] drawerOptions = getResources().getStringArray(R.array.drawer_options_array);
//        drawerIcons = getResources().getIntArray(R.array.drawer_icons_array);
        TypedArray tArray = getResources().obtainTypedArray(R.array.drawer_icons_array);
        int count = tArray.length();
        int[] drawerIcons = new int[count];
        for (int i = 0; i < drawerIcons.length; i++) {
            drawerIcons[i] = tArray.getResourceId(i, 0);
        }
        //Recycles the TypedArray, to be re-used by a later caller.
        //After calling this function you must not ever touch the typed array again.
        tArray.recycle();

        mDrawerLayout = findViewById(R.id.drawer_layout);
        ListView mDrawerList = findViewById(R.id.drawer_list);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new DrawerAdapter(this, drawerOptions, drawerIcons));

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    loadPanels(inProgressAssignments, defaultSortIndex);
                } else if (position == 1) {
                    loadPanels(completedAssignments, defaultSortIndex);
                } else if (position == 2) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                }
                mDrawerLayout.closeDrawers();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        this.menu = menu;
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int id = 0;
        switch (defaultSortIndex) {
            case 0:
                id = R.id.action_sort_by_date;
                break;
            case 1:
                id = R.id.action_sort_by_class;
                break;
            case 2:
                id = R.id.action_sort_by_title;
                break;
            case 3:
                id = R.id.action_sort_by_type;
                break;
        }
        MenuItem item = menu.findItem(id);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        loadPanels(inProgressAssignments, currentSortIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        readSettings();
        invalidateOptionsMenu();
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ArrayList<Assignment> assignments;
        if (getTitle().toString().equals(getResources().getString(R.string.header_completed)))
            assignments = completedAssignments;
        else
            assignments = inProgressAssignments;

        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(Gravity.START);
                return true;
//            case R.id.action_search:
            //TODO: add search function
//                return true;

            case R.id.action_sort_by_date:
                loadPanels(assignments, 0);
                return true;

            case R.id.action_sort_by_class:
                loadPanels(assignments, 1);
                return true;

            case R.id.action_sort_by_type:
                loadPanels(assignments, 2);
                return true;

            case R.id.action_sort_by_title:
                loadPanels(assignments, 3);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.v("MainActivity", "intent action " + intent.getAction());
        if (MARK_DONE.equals(intent.getAction())) {
            Assignment doneAssignment = new Assignment(intent.getExtras());
            for (Assignment assignment : inProgressAssignments) {
                if (doneAssignment.equals(assignment)) {
                    assignment.completed = true;
                }
            }
            loadPanels(inProgressAssignments, defaultSortIndex);
            writeAssignmentsToFile();
        }
        super.onNewIntent(intent);
    }

    public void readSettings() {
        ObjectInputStream inputStream;

        try {
            File file = new File(getFilesDir(), SETTINGS_FILE_NAME);
            inputStream = new ObjectInputStream(new FileInputStream(file));

            defaultSortIndex = inputStream.readInt();
            overdueFirst = inputStream.readBoolean();
            timeEnabled = inputStream.readBoolean();
            notificationTime = (Calendar) inputStream.readObject();

            Log.v("MainActivity", "timeEnabled=" + timeEnabled);

            if (notificationTime == null) {
                notificationTime = Calendar.getInstance();
                notificationTime.set(0, 0, 0, 8, 0);
            }

            alarmHourOfDay = notificationTime.get(Calendar.HOUR_OF_DAY);
            alarmMinuteOfDay = notificationTime.get(Calendar.MINUTE);


            Log.v("MainActivity", "overdueFirst=" + overdueFirst);

            inputStream.close();
        } catch (EOFException e) {
            Log.v("MainActivity.read", "End of stream reached.");
        } catch (IOException e) {
            Log.v("MainActivity.read", "The file was not to be found.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //TODO: enable new read implementation
    public void readAssignmentsFromFile() {
//        try {
//            readAssignments();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
        inProgressAssignments = new ArrayList<>();
        completedAssignments = new ArrayList<>();
        ObjectInputStream inputStream;

        boolean cont = true;
        try {
            File file = new File(getFilesDir(), ASSIGNMENTS_FILE_NAME);
            inputStream = new ObjectInputStream(new FileInputStream(file));
            while (cont) {
                Assignment obj = (Assignment) inputStream.readObject();
                if (obj != null)
                    addAssignment(obj);
                else
                    cont = false;
            }
            inputStream.close();
        } catch (EOFException e) {
            Log.v("MainActivity.read", "End of stream reached.");
        } catch (ClassNotFoundException e) {
            Log.v("MainActivity.read", "No more objects to be had in the file.");
        } catch (IOException e) {
            Log.v("MainActivity.read", "The file was not to be found.");
            e.printStackTrace();
        }
    }

    public void addAssignment(Assignment assignment) {
        if (assignment.dueDate == null) {
            Log.v("MA", "null dateView");
        } else {
            if (assignment.completed)
                completedAssignments.add(assignment);
            else
                inProgressAssignments.add(assignment);
        }
    }

    public void writeAssignmentsToFile() {
        //TODO: enable new read write implementation
//        try {
//            writeAssignments();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try {
            File file = new File(getFilesDir(), ASSIGNMENTS_FILE_NAME);
            boolean fileCreated = file.createNewFile();
            if (fileCreated)
                Log.v("MA", "File did not exist and was created.");

            FileOutputStream fos = new FileOutputStream(file, false);
            try {
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                ArrayList<Assignment> assignments = new ArrayList<>();
                assignments.addAll(inProgressAssignments);
                assignments.addAll(completedAssignments);
                for (Assignment o : assignments) {
                    try {
                        oos.writeObject(o);
                    } catch (NotSerializableException e) {
                        Log.v("MA", "An object was not serializable, it has not been saved.");
                        e.printStackTrace();
                    }
                }

                oos.close();
                fos.close();
            } catch (IOException e) {
                Log.v("MA", "File did not process");
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            Log.v("MA", "File not found to write");
            e.printStackTrace();
        } catch (IOException e) {
            Log.v("MA", "Could not create file for some reason");
            e.printStackTrace();
        }

    }

    public void loadPanels(Assignment assignment, int sortIndex) {
        if (assignment.completed) {
            loadPanels(completedAssignments, sortIndex);
        } else {
            loadPanels(inProgressAssignments, sortIndex);
        }
    }

    public void loadPanels(ArrayList<Assignment> assignments, int sortIndex) {
        setNotificationTimers();
        currentSortIndex = sortIndex;
        if (assignments == inProgressAssignments) {
            setTitle(getResources().getString(R.string.header_in_progress));
            myToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        } else if (assignments == completedAssignments) {
            setTitle(getResources().getString(R.string.header_completed));
            myToolbar.setBackgroundColor(getResources().getColor(R.color.colorCompleted));
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorCompletedDark));
        } else {
            setTitle("Gravy");
            myToolbar.setBackgroundColor(Color.YELLOW);
        }
        for (View view : currentViews) {
            parent.removeView(view);
        }
        currentViews.clear();

        //TODO: Add pinned assignments
        if (assignments.isEmpty()) {
            if (assignments == completedAssignments) {
                addHeading(R.string.no_completed_assignments);
            }
            if (assignments == inProgressAssignments) {
                addHeading(R.string.no_upcoming_assignments);
            }
        } else {

            switch (sortIndex) {
                case 0:
                    sortViewsByDate(assignments);
                    break;
                case 1:
                    sortViewsByClass(assignments);
                    break;
                case 2:
                    sortViewsByType(assignments);
                    break;
                case 3:
                    sortViewsByTitle(assignments);
                    break;
            }
        }
        addHeading(" ");
    }

    //Assignments Sorting methods

    void addHeading(int textID) {
        TextView header = (TextView) getLayoutInflater().inflate(
                R.layout.view_sort_header,
                (ViewGroup) findViewById(android.R.id.content),
                false
        );
        header.setText(textID);
        currentViews.add(header);
        parent.addView(header);
    }

    void addHeading(String text) {
        if (text.equals(""))
            text = "Untitled";
        TextView header = (TextView) getLayoutInflater().inflate(
                R.layout.view_sort_header,
                (ViewGroup) findViewById(android.R.id.content),
                false
        );
        header.setText(text);
        currentViews.add(header);
        parent.addView(header);
    }

    void sortViewsByDate(ArrayList<Assignment> assignments) {

        Calendar today = Calendar.getInstance();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DATE, 1);

        Collections.sort(assignments);
        Assignment previous = assignments.get(0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US);
        ArrayList<Assignment> overdue = new ArrayList<>();

        if (overdueFirst || compareCalendars(previous.dueDate, today) >= 0)
            addDateHeading(dateFormat, today, tomorrow, previous.dueDate);

        for (Assignment assignment : assignments) {
            if (!overdueFirst && compareCalendars(assignment.dueDate, today) < 0) {
                System.out.println("Added assignment to overdue: " + assignment.title);
                overdue.add(assignment);
            } else {
                if (compareCalendars(assignment.dueDate, previous.dueDate) > 0)
                    addDateHeading(dateFormat, today, tomorrow, assignment.dueDate);

                previous = assignment;

                AssignmentViewWrapper assignmentViewWrapper = new AssignmentViewWrapper(
                        this, assignment, currentSortIndex);
                parent.addView(assignmentViewWrapper.container);
                currentViews.add(assignmentViewWrapper.container);
            }
        }

        if (!overdueFirst) {
            if (!overdue.isEmpty())
                addHeading(R.string.due_overdue);
            for (Assignment assignment : overdue) {
                AssignmentViewWrapper assignmentViewWrapper = new AssignmentViewWrapper(
                        this, assignment, currentSortIndex);
                parent.addView(assignmentViewWrapper.container);
                currentViews.add(assignmentViewWrapper.container);
            }
        }

    }

    void addDateHeading(SimpleDateFormat dateFormat, Calendar today, Calendar tomorrow, Calendar date) {
        int compareToToday = compareCalendars(date, today);
        int compareToTomorrow = compareCalendars(date, tomorrow);
        if (compareToToday == 0) {
            addHeading(R.string.due_today);
        } else if (compareToTomorrow == 0) {
            addHeading(R.string.due_tomorrow);
        } else {
            addHeading(dateFormat.format(date.getTime()));
        }
    }

    void sortViewsByClass(ArrayList<Assignment> assignments) {
        ArrayList<String> headings = new ArrayList<>();
        for (Assignment assignment : assignments) {
            if (!headings.contains(assignment.className)) {
                headings.add(assignment.className);
            }
        }
        Collections.sort(headings);
        for (String heading : headings) {
            addHeading(heading);
            for (Assignment assignment : assignments) {
                if (assignment.className.equals(heading)) {
                    AssignmentViewWrapper view = new AssignmentViewWrapper(
                            this, assignment, currentSortIndex);
                    parent.addView(view.container);
                    currentViews.add(view.container);
                }
            }
        }
    }

    void sortViewsByType(ArrayList<Assignment> assignments) {
        String[] types = getResources().getStringArray(R.array.assignment_types_array);
        Collections.sort(assignments);
        for (String type : types) {
            addHeading(type);
            for (Assignment assignment : assignments) {
                if (assignment.type.equals(type)) {
                    AssignmentViewWrapper view = new AssignmentViewWrapper(
                            this, assignment, currentSortIndex);
                    parent.addView(view.container);
                    currentViews.add(view.container);
                }
            }
        }
    }

    void sortViewsByTitle(ArrayList<Assignment> assignments) {
        for (int i = 0; i < assignments.size(); i++) {
            int pos = i;
            for (int j = i; j < assignments.size(); j++) {
                if (assignments.get(j).title.compareTo(assignments.get(pos).title) < 0) {
                    pos = j;
                }
            }

            Assignment min = assignments.get(pos);
            assignments.set(pos, assignments.get(i));
            assignments.set(i, min);

            AssignmentViewWrapper viewContainer = new AssignmentViewWrapper(
                    this, min, currentSortIndex
            );
            parent.addView(viewContainer.container);
            currentViews.add(viewContainer.container);
        }

    }

    public int compareCalendars(Calendar c1, Calendar c2) {
        if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR))
            return c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR);
        if (c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH))
            return c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH);
        return c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Runs when "+" button is pressed
     * Creates new oldAssignment dialog
     *
     * @param view Plus button view
     */
    //TODO: pop-up with extra options, define classes, class colors
    public void createNew(View view) {
        final NewAssignmentDialog newAssignmentDialog = new NewAssignmentDialog();
        Bundle args = new Bundle();
        args.putInt("sortIndex", currentSortIndex);
        args.putBoolean("timeEnabled", timeEnabled);
        newAssignmentDialog.setArguments(args);
        newAssignmentDialog.show(getFragmentManager(), "NewAssignmentDialog");
    }

//    public void openFABMenu(View view) {
//        FloatingActionButton editClasses = (FloatingActionButton) findViewById(R.id.action_edit_classes);
//        FloatingActionButton customAssignment = (FloatingActionButton) findViewById(R.id.action_custom_assignment);
//
//        Animation button1Animation = AnimationUtils.loadAnimation(getApplication(), R.anim.show_button_1);
//    }

    public void deleteAssignment(Assignment assignment, int sortIndex) {
        if (assignment.completed) {
            completedAssignments.remove(assignment);
            writeAssignmentsToFile();
            loadPanels(completedAssignments, sortIndex);
        } else {
            inProgressAssignments.remove(assignment);
            writeAssignmentsToFile();
            loadPanels(inProgressAssignments, sortIndex);
        }
    }


    void setNotificationTimers() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        for (Assignment assignment : inProgressAssignments) {
            setNotificationTimer(assignment, alarmManager);
        }

    }

    void setNotificationTimer(Assignment assignment, AlarmManager alarmManager) {
        PendingIntent pendingIntent = AlarmBroadcastReceiver.createPendingIntent(
                assignment, assignment.hashCode(), timeEnabled, getApplicationContext());
        alarmManager.cancel(pendingIntent);
        long time = alarmTimeFromAssignment(assignment);
        if (time > Calendar.getInstance().getTimeInMillis()) {
//        long time = 0;
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }
    }

    /**
     * returns the date, in milliseconds, at x daysBeforeDueDate
     * at time alarmHourOfDay : alarmMinuteOfDay
     *
     * @param assignment assignment to retrieve date from
     * @return time for alarm to go off, in milliseconds
     */
    long alarmTimeFromAssignment(Assignment assignment) {
        Calendar dueDate = assignment.dueDate;
        Calendar date = new GregorianCalendar();
        date.set(dueDate.get(Calendar.YEAR), dueDate.get(Calendar.MONTH),
                dueDate.get(Calendar.DATE) - daysBeforeDueDate);
        date.set(Calendar.HOUR_OF_DAY, alarmHourOfDay);
        date.set(Calendar.MINUTE, alarmMinuteOfDay);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        return date.getTimeInMillis();
    }

    public static Activity getActivity() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Class activityThreadClass = Class.forName("android.app.ActivityThread");
        Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
        Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
        activitiesField.setAccessible(true);

        Map<Object, Object> activities = (Map<Object, Object>) activitiesField.get(activityThread);
        if (activities == null)
            return null;

        for (Object activityRecord : activities.values()) {
            Class activityRecordClass = activityRecord.getClass();
            Field pausedField = activityRecordClass.getDeclaredField("paused");
            pausedField.setAccessible(true);
            if (!pausedField.getBoolean(activityRecord)) {
                Field activityField = activityRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Activity activity = (Activity) activityField.get(activityRecord);
                return activity;
            }
        }

        return null;
    }


    private void checkFirstRun() {

        final String PREFS_NAME = "MyPrefsFile";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;

        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {

            // This is just a normal run
            return;

        } else if (savedVersionCode == DOESNT_EXIST) {

            // TODO This is a new install (or the user cleared the shared preferences)
            return;

        } else if (currentVersionCode > savedVersionCode) {

            // TODO This is an upgrade
            readAssignmentsOld();

        }

        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
    }

    void readAssignmentsOld() {

    }

    void writeAssignments() throws IOException {
        File file = new File(getFilesDir(), ASSIGNMENTS_FILE_NAME);
        if (file.createNewFile())
            Log.v("MainActivity", "writeAssignments() new file created");
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        int totalThings = inProgressAssignments.size() + completedAssignments.size();
        oos.writeInt(totalThings);
        for (Assignment assignment : inProgressAssignments) {
            writeAssignment(assignment, oos);
        }
        Log.v("MainActivity", "File written");
    }

    void writeAssignment(Assignment assignment, ObjectOutputStream oos) throws IOException {
        oos.writeObject(assignment.title);
        oos.writeObject(assignment.className);
        oos.writeObject(assignment.dueDate);
        oos.writeObject(assignment.description);
        oos.writeBoolean(assignment.completed);
        oos.writeObject(assignment.type);
    }

    void readAssignments() throws IOException, ClassNotFoundException {
        File file = new File(getFilesDir(), ASSIGNMENTS_FILE_NAME);
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);

        int total = ois.readInt();
        for (int i = 0; i < total; i++) {
            addAssignment(readAssignment(ois));
        }
        Log.v("MainActivity", "readAssignments()");
    }

    Assignment readAssignment(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        return new Assignment(
                (String) ois.readObject(), //title
                (String) ois.readObject(), //className
                (Calendar) ois.readObject(), //dueDate
                (String) ois.readObject(), //description
                ois.readBoolean(), //completed
                (String) ois.readObject() //type
        );
    }
} // end MainActivity class