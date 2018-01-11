package go.planner.plannergo;

import android.app.DatePickerDialog;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
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

    //Stores assignments in completed/not completed sets
    ArrayList<Assignment> inProgressAssignments;
    ArrayList<Assignment> completedAssignments;

    ArrayList<View> currentViews = new ArrayList<>();

    LinearLayout parent;

    DatePickerDialog datePickerDialog;
    SimpleDateFormat dateFormatter;

    final String fileName = "assignmentsFile";

    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        parent = (LinearLayout) findViewById(R.id.body);

        setUpNavDrawer();

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
//                for (Assignment assignment : completedAssignments)
//                    assignment.completed = true;
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

    public void loadPanels() {
        loadPanels(inProgressAssignments);
    }

    public void loadPanels(ArrayList<Assignment> assignments) {
//        Log.v("MA", "inProgressView=" + inProgressView);
        for (View view : currentViews) {
            parent.removeView(view);
        }
        currentViews.clear();

        //TODO: Add pinned assignments

        //TODO: Add sorting options
        sortByDatesWithHeaders(assignments);

    }

    void sortByDatesWithHeaders(ArrayList<Assignment> assignments) {
        addHeading(R.string.due_today);
        addHeading(R.string.due_tomorrow);
        addHeading(R.string.due_upcoming);
        addHeading(R.string.due_overdue);

        ArrayList<AssignmentCheckBox> dueToday = new ArrayList<>();
        ArrayList<AssignmentCheckBox> dueTomorrow = new ArrayList<>();
        ArrayList<AssignmentCheckBox> dueUpcoming = new ArrayList<>();
        ArrayList<AssignmentCheckBox> dueOverdue = new ArrayList<>();

        Calendar today = Calendar.getInstance();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DATE, 1);

        for (Assignment assignment : assignments) {
            int compareToToday = compareCalendars(assignment.dueDate, today);
            AssignmentCheckBox assignmentCheckBox = new AssignmentCheckBox(
                    this, assignment, getFragmentManager()
            );
            if (compareToToday == 0)
                dueToday.add(assignmentCheckBox);
            else if (compareToToday > 0) {
                int compareToTomorrow = compareCalendars(assignment.dueDate, tomorrow);
                if (compareToTomorrow == 0)
                    dueTomorrow.add(assignmentCheckBox);
                else
                    dueUpcoming.add(assignmentCheckBox);
            } else
                dueOverdue.add(assignmentCheckBox);
        }

        displayViewsByDate(dueOverdue, 4);
        displayViewsByDate(dueUpcoming, 3);
        displayViewsByDate(dueTomorrow, 2);
        displayViewsByDate(dueToday, 1);
    }

    void addHeading(int textID) {
        TextView header = (TextView) getLayoutInflater().inflate(
                R.layout.sorting_header,
                (ViewGroup) findViewById(android.R.id.content),
                false
        );
        header.setText(textID);
        currentViews.add(header);
        parent.addView(header);
    }

    void displayViewsByDate(ArrayList<AssignmentCheckBox> assignmentCheckBoxes, int index) {
        Collections.sort(assignmentCheckBoxes);
        Collections.reverse(assignmentCheckBoxes);
        for (AssignmentCheckBox assignmentCheckBox : assignmentCheckBoxes) {
            parent.addView(assignmentCheckBox.container, index);
            currentViews.add(assignmentCheckBox.container);
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
     * Creates new assignment dialog
     *
     * @param view Plus button view
     */
    public void createNew(View view) {
        final NewAssignmentDialog newAssignmentDialog = new NewAssignmentDialog();

        newAssignmentDialog.show(getFragmentManager(), "NewAssignmentDialog");

        //DatePicker test code
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);


        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);

                newAssignmentDialog.calendar = newDate;
                newAssignmentDialog.dateView.setText(dateFormatter.format(newDate.getTime()));
            }

        }, year, month, day);
    }

    public void changeDate(View view) {
        datePickerDialog.show();
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

