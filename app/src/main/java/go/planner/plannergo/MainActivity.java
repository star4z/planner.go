package go.planner.plannergo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

    //Settings data
    SharedPreferences sharedPref;
    int currentSortIndex = 0;

    //Quick references
    LinearLayout parent;
    Toolbar myToolbar;
    private DrawerLayout mDrawerLayout;

    //TODO: add tutorial

    /**
     * Runs when the activity is created
     *
     * @param savedInstanceState stores state for restoring the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parent = findViewById(R.id.body);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        currentSortIndex = 0;

        myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

//        checkFirstRun();

        FileIO.readAssignmentsFromFile(this);

    }


    /**
     * Runs when the activity becomes active (again)
     */
    @Override
    protected void onResume() {

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ColorPicker.setColors(this);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setImageResource((ColorPicker.getColorSecondaryText() == Color.BLACK) ?
                R.drawable.ic_add_black_24dp : R.drawable.ic_add_white_24dp);
        fab.setBackgroundTintList(ColorStateList.valueOf(ColorPicker.getColorSecondary()));
        fab.setRippleColor(ColorPicker.getColorSecondary());
        invalidateOptionsMenu();
        setUpNavDrawer();
        loadPanels(FileIO.inProgressAssignments, currentSortIndex);
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


        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    loadPanels(FileIO.inProgressAssignments);
                } else if (position == 1) {
                    loadPanels(FileIO.completedAssignments);
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
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ArrayList<NewAssignment> assignments;
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
//            case R.id.action_open_classes:
//                startActivity(new Intent(this, ClassActivity.class));
//                return true;
//            case R.id.action_open_types:
//                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    void loadPanels(ArrayList<NewAssignment> assignments) {
        loadPanels(assignments, 0);
    }

    void loadPanels(NewAssignment assignment) {
        loadPanels(assignment, 0);
    }


    public void loadPanels(NewAssignment assignment, int sortIndex) {
        if (assignment.completed) {
            loadPanels(FileIO.completedAssignments, sortIndex);
        } else {
            loadPanels(FileIO.inProgressAssignments, sortIndex);
        }
    }

    public void loadPanels(ArrayList<NewAssignment> assignments, int sortIndex) {
        NotificationAlarms.setNotificationTimers(this);

        currentSortIndex = sortIndex;
        if (assignments == FileIO.inProgressAssignments) {
            setTitle(getResources().getString(R.string.header_in_progress));
            myToolbar.setBackgroundColor(ColorPicker.getColorPrimary());
            myToolbar.setTitleTextColor(ColorPicker.getColorPrimaryText());
            getWindow().setStatusBarColor(ColorPicker.getColorPrimaryAccent());
            myToolbar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(),
                    (ColorPicker.getColorPrimaryText() == Color.BLACK)
                            ? R.drawable.ic_more_vert_black_24dp
                            : R.drawable.ic_more_vert_white_24dp));
            myToolbar.setNavigationIcon((ColorPicker.getColorPrimaryText() == Color.BLACK)
                    ? R.drawable.ic_dehaze_black_24dp : R.drawable.ic_dehaze_white_24dp);
        } else if (assignments == FileIO.completedAssignments) {
            setTitle(getResources().getString(R.string.header_completed));
            myToolbar.setBackgroundColor(ColorPicker.getColorCompleted());
            myToolbar.setTitleTextColor(ColorPicker.getColorCompletedText());
            getWindow().setStatusBarColor(ColorPicker.getColorCompletedAccent());
            myToolbar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(),
                    (ColorPicker.getColorCompletedText() == Color.BLACK)
                            ? R.drawable.ic_more_vert_black_24dp
                            : R.drawable.ic_more_vert_white_24dp));
            myToolbar.setNavigationIcon((ColorPicker.getColorCompletedText() == Color.BLACK)
                    ? R.drawable.ic_dehaze_black_24dp : R.drawable.ic_dehaze_white_24dp);
        } else {
            //Should never run. "Should."
            setTitle("Gravy");
            myToolbar.setBackgroundColor(Color.YELLOW);
        }

        parent.removeAllViews();

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
        parent.addView(header);
    }

    void sortViewsByDate(ArrayList<NewAssignment> assignments) {

        ArrayList<NewAssignment> priorityAssignments = new ArrayList<>();
        for (NewAssignment assignment : assignments) {
            if (assignment.priority > 0) {
                priorityAssignments.add(assignment);
            }
        }
        if (!priorityAssignments.isEmpty()) {
            addHeading("Priority");
            Collections.sort(priorityAssignments);
            for (NewAssignment assignment : priorityAssignments)
                parent.addView(new AssignmentViewWrapper(
                        this, assignment, currentSortIndex).container);

        }


        Calendar today = Calendar.getInstance();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DATE, 1);

        Collections.sort(assignments);
        NewAssignment previous = assignments.get(0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US);
        ArrayList<NewAssignment> overdue = new ArrayList<>();

        if (!sharedPref.getBoolean(SettingsActivity.overdueLast, false) || compareCalendars(previous.dueDate, today) >= 0)
            addDateHeading(dateFormat, today, tomorrow, previous.dueDate);

        for (NewAssignment assignment : assignments) {
            if (sharedPref.getBoolean(SettingsActivity.overdueLast, false) && compareCalendars(assignment.dueDate, today) < 0) {
                overdue.add(assignment);
            } else {
                if (compareCalendars(assignment.dueDate, previous.dueDate) > 0)
                    addDateHeading(dateFormat, today, tomorrow, assignment.dueDate);

                previous = assignment;

                AssignmentViewWrapper assignmentViewWrapper = new AssignmentViewWrapper(
                        this, assignment, currentSortIndex);
                parent.addView(assignmentViewWrapper.container);
            }
        }

        if (sharedPref.getBoolean(SettingsActivity.overdueLast, false)) {
            if (!overdue.isEmpty())
                addHeading(R.string.due_overdue);
            for (NewAssignment assignment : overdue) {
                AssignmentViewWrapper assignmentViewWrapper = new AssignmentViewWrapper(
                        this, assignment, currentSortIndex);
                parent.addView(assignmentViewWrapper.container);
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

    void sortViewsByClass(ArrayList<NewAssignment> assignments) {

        ArrayList<String> headings = new ArrayList<>();
        for (NewAssignment assignment : assignments) {
            if (!headings.contains(assignment.className)) {
                headings.add(assignment.className);
            }
        }
        Collections.sort(headings);
        for (String heading : headings) {
            addHeading(heading);
            for (NewAssignment assignment : assignments) {
                if (assignment.className.equals(heading)) {
                    AssignmentViewWrapper view = new AssignmentViewWrapper(
                            this, assignment, currentSortIndex);
                    parent.addView(view.container);
                }
            }
        }
    }

    void sortViewsByType(ArrayList<NewAssignment> assignments) {

        String[] types = getResources().getStringArray(R.array.assignment_types_array);
        Collections.sort(assignments);
        for (String type : types) {
            addHeading(type);
            for (NewAssignment assignment : assignments) {
                if (assignment.type.equals(type)) {
                    AssignmentViewWrapper view = new AssignmentViewWrapper(
                            this, assignment, currentSortIndex);
                    parent.addView(view.container);
                }
            }
        }
    }

    void sortViewsByTitle(ArrayList<NewAssignment> assignments) {
        for (int i = 0; i < assignments.size(); i++) {
            int pos = i;
            for (int j = i; j < assignments.size(); j++) {
                if (assignments.get(j).title.compareTo(assignments.get(pos).title) < 0) {
                    pos = j;
                }
            }

            NewAssignment min = assignments.get(pos);
            assignments.set(pos, assignments.get(i));
            assignments.set(i, min);
        }

        char currentLetter = 0;
        boolean emptyLetter = true; //helps with checking that blank character is added only once
        for (NewAssignment assignment : assignments) {
            if (assignment.title.length() == 0) {
                if (emptyLetter) {
                    addHeading("Untitled");
                    emptyLetter = false;
                }
            } else if (assignment.title.toUpperCase().charAt(0) > currentLetter) {
                currentLetter = assignment.title.charAt(0);
                addHeading(Character.toString(currentLetter).toUpperCase());
            }
            AssignmentViewWrapper viewContainer = new AssignmentViewWrapper(
                    this, assignment, currentSortIndex
            );
            parent.addView(viewContainer.container);
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
     * Creates new oldNewAssignment dialog
     *
     * @param view Plus button view
     */
    //TODO: pop-up with extra options, define classes, class colors
    public void createNew(View view) {
        startActivity(new Intent(MainActivity.this, NewAssignmentActivity.class));
    }
} // end MainActivity class