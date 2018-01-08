package go.planner.plannergo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
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
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    HashMap<Assignment, AssignmentCheckBox> assignmentViewHashMap = new HashMap<>();
    ArrayList<Assignment> assignments = new ArrayList<>();
    ArrayList<Assignment> completedAssignments = new ArrayList<>();
    LinearLayout parent;

    //EditText date
    DatePickerDialog datePickerDialog;
    SimpleDateFormat dateFormatter;

    final String fileName = "assignmentsFile";

    int today = 1;
    int tomorrow = 2;
    int upcoming = 3;
    int overdue = 4;

    private String[] drawerOptions;
    private int[] drawerIcons;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        parent = (LinearLayout) findViewById(R.id.body);

        drawerOptions = getResources().getStringArray(R.array.drawer_options_array);
//        drawerIcons = getResources().getIntArray(R.array.drawer_icons_array);
        TypedArray tArray = getResources().obtainTypedArray(R.array.drawer_icons_array);
        int count = tArray.length();
        drawerIcons = new int[count];
        for (int i = 0; i < drawerIcons.length; i++) {
            drawerIcons[i] = tArray.getResourceId(i, 0);
        }
        //Recycles the TypedArray, to be re-used by a later caller.
        //After calling this function you must not ever touch the typed array again.
        tArray.recycle();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.drawer_list);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new DrawerAdapter(this, drawerOptions, drawerIcons));

        // TODO:Set the list's click listener
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        assignments = readAssignmentsFromFile();
        for (Assignment assignment : assignments) {
            addAssignmentToLayout(assignment);
        }
    }

    public ArrayList<Assignment> readAssignmentsFromFile() {
        ArrayList<Assignment> assignments = new ArrayList<>();
        ObjectInputStream inputStream;

        boolean cont = true;
        try {
            File file = new File(getFilesDir(), fileName);
            inputStream = new ObjectInputStream(new FileInputStream(file));
            while (cont) {
                Assignment obj = (Assignment) inputStream.readObject();
                if (obj != null)
                    assignments.add(obj);
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
        return assignments;
    }

    public void addAssignmentToLayout(Assignment assignment) {

        AssignmentCheckBox box = new AssignmentCheckBox(this, assignment);
        assignmentViewHashMap.put(assignment, box);
        writeAssignmentsToFile();

        //Adds box in appropriate section
        int addAtIndex = parentDateIndex(box);

        //TODO: Add assignment type sorting options

        //TODO: Sort list from oldest to newest (Today or newer is first)
        //Use ArrayLists for each section and add them in reverse order (i.e. overdue first)


        parent.addView(box.container, addAtIndex);
    }

    int parentDateIndex(AssignmentCheckBox box){
        int addAtIndex;

        Calendar today = Calendar.getInstance();

        int compareToToday = compareCalendars(box.assignment.dueDate, today);
        if (compareToToday == 0) {
            Log.v("", "Added at today");
            addAtIndex = this.today;
            tomorrow++;
            upcoming++;
            overdue++;
        } else if (compareToToday < 0) {
            Log.v("", "Added at overdue");
            addAtIndex = overdue;
        } else {
            //set today as tomorrow
            today.add(Calendar.DATE, 1);
            if (compareCalendars(box.assignment.dueDate, today) == 0) {
                addAtIndex = tomorrow;
                upcoming++;
                overdue++;
            } else {
                addAtIndex = upcoming;
                overdue++;
            }
        }

        return addAtIndex;
    }

    public void checked(View view) {
        View viewParent = (View) view.getParent();
        parent.removeView(viewParent);
        getAssignmentFromView(viewParent);
        assignments.remove(assignment);
        writeAssignmentsToFile();
    }

    public int compareCalendars(Calendar c1, Calendar c2) {
        if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR))
            return c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR);
        if (c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH))
            return c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH);
        return c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH);
    }

    public void writeAssignmentsToFile() {
        try {
            File file = new File(getFilesDir(), fileName);
            boolean fileCreated = file.createNewFile();
            if(fileCreated)
                Log.v("MA","File did not exist and was created.");

            FileOutputStream fos = new FileOutputStream(file, false);
            try {
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                for (Object o : assignments) {
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


    /**
     * Runs when "+" button is pressed
     * Creates new assignment dialog
     *
     * @param view Plus button view
     */
    public void addNewEvent(View view) {
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
                newAssignmentDialog.date.setText(dateFormatter.format(newDate.getTime()));
            }

        }, year, month, day);
    }

    public void changeDate(View view) {
        datePickerDialog.show();
    }

    public static class NewAssignmentDialog extends DialogFragment {

        private final boolean updateOnCreate;
        EditText title, cName, date, descr;
        Calendar calendar;
        Assignment assignment;
        boolean created = false;
        View view;

        public NewAssignmentDialog() {
            super();
            updateOnCreate = false;
        }


        @SuppressLint("ValidFragment")
        public NewAssignmentDialog(boolean updateOnCreate, View view) {
            super();

            this.updateOnCreate = updateOnCreate;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();


            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            final View view = inflater.inflate(R.layout.new_assignment_dialog, null);

            //Intialize EditTexts
            title = (EditText) view.findViewById(R.id.hw_title);
            cName = (EditText) view.findViewById(R.id.hw_class);
            date = (EditText) view.findViewById(R.id.hw_due_date);
            descr = (EditText) view.findViewById(R.id.hw_description);

            if (updateOnCreate)
                updateViews(((MainActivity) getActivity()).getAssignment());
            //Build AlertDialog components
            builder.setView(view)
                    //TODO: edit texts based on updateOnCreate to indicate edit or create
                    .setTitle(R.string.new_a)
                    // Add action buttons
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            created = true;
                            //add new assignment to page
                            assignment = new Assignment(
                                    title.getText().toString(),
                                    cName.getText().toString(),
                                    calendar,
                                    descr.getText().toString());
                            if (updateOnCreate) {
                                ((MainActivity) getActivity()).deleteAssignment(view);
                                dismiss();
                            }
                            ((MainActivity) getActivity()).assignments.add(assignment);
                            ((MainActivity) getActivity()).addAssignmentToLayout(assignment);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            NewAssignmentDialog.this.getDialog().cancel();
                        }
                    });

            SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            calendar = Calendar.getInstance();

            date.setText(dateFormatter.format(calendar.getTime()));

            return builder.create();
        } // end onCreateDialog()


        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        public void updateViews(Assignment assignment) {
            title.setText(assignment.title);
            cName.setText(assignment.className);
            date.setText(
                    ((MainActivity) getActivity()).dateFormatter.format(assignment.dueDate.getTime())
            );
            descr.setText(assignment.description);
        }
    } //end NewAssignmentDialog class

    Assignment assignment;
    AssignmentDetailsDialog detailsDialog;

    public Assignment getAssignment() {
        return assignment;
    }

    public void showDetails(View view) {
        getAssignmentFromView(view);
        detailsDialog = new AssignmentDetailsDialog();
        detailsDialog.show(getFragmentManager(), null);
    }

    public static class AssignmentDetailsDialog extends DialogFragment {
        //TODO: add View constructor and implement with Delete (and maybe Edit) function so that deleteAssignment(View) works like checked

        //        Assignment assignment;
        TextView title, className, date, description;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = getActivity().getLayoutInflater();

            View view = inflater.inflate(R.layout.assignment_details_dialog, null);

            title = (TextView) view.findViewById(R.id.title);

            className = (TextView) view.findViewById(R.id.class_name);
            date = (TextView) view.findViewById(R.id.date);
            description = (TextView) view.findViewById(R.id.description);

            updateViews();

            builder.setView(view);

            return builder.create();

        }


        public void updateViews() {
            Assignment assignment = ((MainActivity) getActivity()).getAssignment();
            title.setText(assignment.title);
            className.setText(assignment.className);
            date.setText(
                    ((MainActivity) getActivity()).dateFormatter.format(assignment.dueDate.getTime())
            );
            description.setText(assignment.description);
        }
    }

    public void deleteAssignment(View view) {
        View grandparent = (View) (view.getParent().getParent());
        getAssignmentFromView((View) (view.getParent()).getParent());
        assignments.remove(assignment);
        writeAssignmentsToFile();
        detailsDialog.dismiss();
        recreate();
    }

    public void editAssignment(View view) {
        final NewAssignmentDialog newAssignmentDialog = new NewAssignmentDialog(true, view);

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
                newAssignmentDialog.date.setText(dateFormatter.format(newDate.getTime()));
            }

        }, year, month, day);

        getAssignmentFromView((View) (view.getParent()).getParent().getParent());

//        newAssignmentDialog.updateViews(getAssignment());

        //TODO: Only delete if saved, not cancelled.
        //TODO: (Or) Delete old one if saved. (This script runs as soon as the box is created.)
        Log.v("MA", "created?=" + newAssignmentDialog.created);
    }

    public void closeDetails(View view) {
        detailsDialog.dismiss();
    }

    public boolean getAssignmentFromView(View view) {
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView className = (TextView) view.findViewById(R.id.class_name);

        if (title == null || className == null) {
            Log.v("MA", "Assignment failed to produce title or class fields");
            Log.v("MA", "title=" + title + "\nclassName=" + className);
            return false;
        }

        Assignment newAssignment = new Assignment(
                title.getText().toString(), className.getText().toString(), null, null
        );

        int indexOfAssignment = assignments.indexOf(newAssignment);
        if (indexOfAssignment != -1) {
            assignment = assignments.get(indexOfAssignment);
            return true;
        } else {
            Log.e("MA", "Could not get Assignment from given view");
            return false;
        }
    }

} // end MainActivity class
