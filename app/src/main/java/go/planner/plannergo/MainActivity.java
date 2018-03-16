package go.planner.plannergo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final String MARK_DONE = "app.planner.MARK_DONE";

    //Settings data
//    Bundle settings;
    SharedPreferences sharedPref;
    int currentSortIndex = 0;

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

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

//        setUpNavDrawer();
        checkFirstRun();

//        currentSortIndex = settings.getInt("defaultSortIndex");
        currentSortIndex = SettingsActivity.getInt(
                sharedPref.getString(SettingsActivity.defaultSort, ""), this);

        myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

//        readAssignmentsFromFile();
        FileIO.readAssignmentsFromFile(this);
    }

    @Override
    protected void onResume() {
//        settings = FileIO.readSettings(this);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        invalidateOptionsMenu();
        setUpNavDrawer();
        super.onResume();
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

        final String defaultSort = sharedPref.getString(SettingsActivity.defaultSort, "");

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    loadPanels(FileIO.inProgressAssignments, defaultSort);
                } else if (position == 1) {
                    loadPanels(FileIO.completedAssignments, defaultSort);
                } else if (position == 2) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                }
                mDrawerLayout.closeDrawers();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int id = 0;
        int sort = SettingsActivity.getInt(
                sharedPref.getString(SettingsActivity.defaultSort, ""), this);
        switch (sort) {

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
        loadPanels(FileIO.inProgressAssignments, currentSortIndex);
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ArrayList<Assignment> assignments;
        if (getTitle().toString().equals(getResources().getString(R.string.header_completed)))
            assignments = FileIO.completedAssignments;
        else
            assignments = FileIO.inProgressAssignments;

        loadPanels(assignments, currentSortIndex);

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

    public void loadPanels(ArrayList<Assignment> assignments, String sort) {
        loadPanels(assignments, SettingsActivity.getInt(sort, this));

    }

    public void loadPanels(Assignment assignment, int sortIndex) {
        if (assignment.completed) {
            loadPanels(FileIO.completedAssignments, sortIndex);
        } else {
            loadPanels(FileIO.inProgressAssignments, sortIndex);
        }
    }

    public void loadPanels(ArrayList<Assignment> assignments, int sortIndex) {
        NotificationAlarms.setNotificationTimers(this);

        currentSortIndex = sortIndex;
        if (assignments == FileIO.inProgressAssignments) {
            setTitle(getResources().getString(R.string.header_in_progress));
            myToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        } else if (assignments == FileIO.completedAssignments) {
            setTitle(getResources().getString(R.string.header_completed));
            myToolbar.setBackgroundColor(getResources().getColor(R.color.p1_completed));
            getWindow().setStatusBarColor(getResources().getColor(R.color.p1_completed_dark));
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
            if (assignments == FileIO.completedAssignments) {
                addHeading(R.string.no_completed_assignments);
            }
            if (assignments == FileIO.inProgressAssignments) {
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

        if (!sharedPref.getBoolean(SettingsActivity.overdueLast, false) || compareCalendars(previous.dueDate, today) >= 0)
            addDateHeading(dateFormat, today, tomorrow, previous.dueDate);

        for (Assignment assignment : assignments) {
            if (sharedPref.getBoolean(SettingsActivity.overdueLast, false) && compareCalendars(assignment.dueDate, today) < 0) {
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

        if (sharedPref.getBoolean(SettingsActivity.overdueLast, false)) {
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
        args.putBoolean("timeEnabled", sharedPref.getBoolean(SettingsActivity.timeEnabled, true));
        newAssignmentDialog.setArguments(args);
        newAssignmentDialog.show(getFragmentManager(), "NewAssignmentDialog");
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
            Log.v("MainActivity", "Regular Read");

            return;

        } else if (savedVersionCode == DOESNT_EXIST) {

            Log.v("MainActivity", "New Install");

            return;

        } else if (currentVersionCode > savedVersionCode) {

            // TODO This is an upgrade
            Log.v("MainActivity", "Upgrade");


        }

        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
    }

} // end MainActivity class