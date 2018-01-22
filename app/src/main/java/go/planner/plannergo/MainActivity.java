package go.planner.plannergo;

import android.app.SearchManager;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ArrayList<Assignment> inProgressAssignments;
    ArrayList<Assignment> completedAssignments;
    ArrayList<View> currentViews = new ArrayList<>();
    LinearLayout parent;
    final String fileName = "assignmentsFile";

    Toolbar myToolbar;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parent = (LinearLayout) findViewById(R.id.body);

        setUpNavDrawer();

        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        readAssignmentsFromFile();
        loadPanels(inProgressAssignments);
    }

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

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView mDrawerList = (ListView) findViewById(R.id.drawer_list);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new DrawerAdapter(this, drawerOptions, drawerIcons));

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    loadPanels(inProgressAssignments);
                } else if (position == 1) {
                    loadPanels(completedAssignments);
                }
                mDrawerLayout.closeDrawers();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Associate searchable configuration with the SearchView
       /* SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        assert searchManager != null;
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));*/
        return true;
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

            case R.id.action_default_sort:
                loadPanels(assignments, 0);
                return true;

            case R.id.action_sort_by_date:
                loadPanels(assignments, 1);
                return true;

            case R.id.action_sort_by_class:
                loadPanels(assignments, 2);
                return true;

            case R.id.action_sort_by_type:
                loadPanels(assignments, 3);
                return true;

            case R.id.action_sort_by_title:
                loadPanels(assignments, 4);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void readAssignmentsFromFile() {
        inProgressAssignments = new ArrayList<>();
        completedAssignments = new ArrayList<>();
        ObjectInputStream inputStream;

        boolean cont = true;
        try {
            File file = new File(getFilesDir(), fileName);
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
        try {
            File file = new File(getFilesDir(), fileName);
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

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY).toLowerCase();
            //use the query to search your data somehow

            //organize general gui
            setTitle("Search Results");
            for (View view : currentViews) {
                parent.removeView(view);
            }
            currentViews.clear();

            //ready data members
            ArrayList<Assignment> allAssignments = new ArrayList<>();
            allAssignments.addAll(inProgressAssignments);
            allAssignments.addAll(completedAssignments);
            ArrayList<AssignmentViewContainer> foundInClass = new ArrayList<>();
            ArrayList<Assignment> foundInOther = new ArrayList<>();
            for (Assignment assignment : allAssignments) {
                if (assignment.title.toLowerCase().contains(query)) {
                    AssignmentViewContainer viewContainer = new AssignmentViewContainer(
                            this, assignment
                    );
                    viewContainer.titleView.setTextColor(Color.BLUE);
                    parent.addView(viewContainer.container);
                    currentViews.add(viewContainer.container);
                } else if (assignment.className.toLowerCase().contains(query)) {
                    AssignmentViewContainer viewContainer = new AssignmentViewContainer(
                            this, assignment
                    );
                    viewContainer.classView.setTextColor(Color.BLUE);
                    foundInClass.add(viewContainer);
                } else if (assignment.description.toLowerCase().contains(query)
                        || assignment.type.toLowerCase().contains(query)) {
                    foundInOther.add(assignment);
                }
            }
            for (AssignmentViewContainer container : foundInClass) {
                parent.addView(container.container);
                currentViews.add(container.container);
            }
            for (Assignment assignment : foundInOther) {
                AssignmentViewContainer container = new AssignmentViewContainer(
                        this, assignment);
                parent.addView(container.container);
                currentViews.add(container.container);
            }
        }
    }

    public void loadPanels() {
        loadPanels(inProgressAssignments, 0);
    }

    public void loadPanels(ArrayList<Assignment> assignments) {
        loadPanels(assignments, 0);
    }

    public void loadPanels(ArrayList<Assignment> assignments, int sortID) {
        if (assignments == inProgressAssignments) {
            setTitle(getResources().getString(R.string.header_in_progress));
            myToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        } else if (assignments == completedAssignments) {
            setTitle(getResources().getString(R.string.header_completed));
            myToolbar.setBackgroundColor(getResources().getColor(R.color.colorCompleted));
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorCompletedDark));
        } else
            setTitle("Gravy");
        for (View view : currentViews) {
            parent.removeView(view);
        }
        currentViews.clear();

        //TODO: Add pinned assignments

        switch (sortID) {
            case 0:
                sortViewsByToday(assignments);
                break;
            case 1:
                sortViewsByDate(assignments, true);
                break;
            case 2:
                sortViewsByClass(assignments);
                break;
            case 3:
                sortViewsByType(assignments);
                break;
            case 4:
                sortViewsByTitle(assignments);
                break;
        }
    }

    void sortViewsByToday(ArrayList<Assignment> assignments) {

        ArrayList<Assignment> dueToday = new ArrayList<>();
        ArrayList<Assignment> dueTomorrow = new ArrayList<>();
        ArrayList<Assignment> dueUpcoming = new ArrayList<>();
        ArrayList<Assignment> dueOverdue = new ArrayList<>();

        Calendar today = Calendar.getInstance();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DATE, 1);

        for (Assignment assignment : assignments) {
            int compareToToday = compareCalendars(assignment.dueDate, today);
            if (compareToToday == 0)
                dueToday.add(assignment);
            else if (compareToToday > 0) {
                int compareToTomorrow = compareCalendars(assignment.dueDate, tomorrow);
                if (compareToTomorrow == 0)
                    dueTomorrow.add(assignment);
                else
                    dueUpcoming.add(assignment);
            } else
                dueOverdue.add(assignment);
        }

        boolean empty = true;
        if (!dueToday.isEmpty()) {
            empty = false;
            addHeading(R.string.due_today);
            sortViewsByDate(dueToday, false);
        }
        if (!dueTomorrow.isEmpty()) {
            empty = false;
            addHeading(R.string.due_tomorrow);
            sortViewsByDate(dueTomorrow, false);
        }
        if (!dueUpcoming.isEmpty()) {
            empty = false;
            addHeading(R.string.due_upcoming);
            sortViewsByDate(dueUpcoming, false);
        }
        if (!dueOverdue.isEmpty()) {
            empty = false;
            addHeading(R.string.due_overdue);
            sortViewsByDate(dueOverdue, false);
        }
        if (empty) {
            addHeading(R.string.no_upcoming_assignments);
        }

    }

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

    void sortViewsByDate(ArrayList<Assignment> assignments, boolean headings) {
        if (assignments.isEmpty()) {
            if (assignments == completedAssignments) {
                addHeading(R.string.no_completed_assignments);
            }
            if (assignments == inProgressAssignments) {
                addHeading(R.string.no_upcoming_assignments);
            }

        } else {
            Collections.sort(assignments);
            Assignment previous = null;
            SimpleDateFormat dateFormat = null;
            if (headings) {
                previous = assignments.get(0);
                dateFormat = new SimpleDateFormat("EEE, MMM dd, YYYY", Locale.US);
                addHeading(dateFormat.format(previous.dueDate.getTime()));
            }
            for (Assignment assignment : assignments) {
                if (headings) {
                    if (compareCalendars(assignment.dueDate, previous.dueDate) != 0)
                        addHeading(dateFormat.format(assignment.dueDate.getTime()));
                    previous = assignment;
                }
                AssignmentViewContainer assignmentViewContainer = new AssignmentViewContainer(
                        this, assignment);
                parent.addView(assignmentViewContainer.container);
                currentViews.add(assignmentViewContainer.container);

            }
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
                    AssignmentViewContainer view = new AssignmentViewContainer(
                            this, assignment);
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
                    AssignmentViewContainer view = new AssignmentViewContainer(
                            this, assignment);
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

            AssignmentViewContainer viewContainer = new AssignmentViewContainer(
                    this, min
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
    //TODO: pop-up with extra options, define classes
    public void createNew(View view) {
        final NewAssignmentDialog newAssignmentDialog = new NewAssignmentDialog();
        newAssignmentDialog.show(getFragmentManager(), "NewAssignmentDialog");
    }

    public void openFABMenu(View view) {
        FloatingActionButton editClasses = (FloatingActionButton) findViewById(R.id.action_edit_classes);
        FloatingActionButton customAssignment = (FloatingActionButton) findViewById(R.id.action_custom_assignment);

        Animation button1Animation = AnimationUtils.loadAnimation(getApplication(), R.anim.show_button_1);

    }

    public void deleteAssignment(Assignment assignment) {
        if (assignment.completed) {
            completedAssignments.remove(assignment);
            writeAssignmentsToFile();
            loadPanels(completedAssignments);
        } else {
            inProgressAssignments.remove(assignment);
            writeAssignmentsToFile();
            loadPanels(inProgressAssignments);
        }
    }
} // end MainActivity class