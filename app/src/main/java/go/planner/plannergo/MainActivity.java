package go.planner.plannergo;

import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
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

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //Settings data
    SharedPreferences sharedPref;
    int currentSortIndex = 0;
    boolean currentScreenIsInProgress = true;

    //Quick references
    LinearLayout parent;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;

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

        FileIO.readAssignmentsFromFile(this);

        parent = findViewById(R.id.parent);

//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
//        dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.div_grey)));
//        parent.addItemDecoration(dividerItemDecoration);

//        parent.setLayoutManager(layoutManager);


        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        FileIO.readAssignmentsFromFile(this);

    }


    /**
     * Runs when the activity becomes active (again)
     */
    @Override
    protected void onResume() {
        //Update data
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ColorPicker.setColors(this);
        FileIO.readAssignmentsFromFile(this);

        //Preform necessary resets
        invalidateOptionsMenu();

        //Call GUI setup methods
        initFAB();
        initNavDrawer();
        loadPanels((currentScreenIsInProgress) ? FileIO.inProgressAssignments : FileIO.completedAssignments, currentSortIndex);

        //Call super
        super.onResume();
    }

    /**
     * Since this is a single-top activity, when a startActivity(MainActivity) is called, this
     * method handles any updates, rather than restarting the activity
     *
     * @param intent contains any data that may modify the way the activity runs
     */
    @Override
    protected void onNewIntent(Intent intent) {
        Log.v("MainActivity", "NewIntent");
        if (intent.getExtras() != null) {
            long idToRemove = intent.getExtras().getLong("remove_id", -1);
            Log.v("MainActivity", "idToRemove=" + idToRemove);
            if (idToRemove != -1) {
                FileIO.deleteAssignment(this, FileIO.getAssignment(idToRemove));
            }

            currentScreenIsInProgress = intent.getExtras().getBoolean("mode_InProgress", true);
        }

        super.onNewIntent(intent);
    }

    private void initNavDrawer() {
        String[] drawerOptions = getResources().getStringArray(R.array.drawer_options_array);
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

        mDrawerList.setAdapter(new DrawerAdapter(this, drawerOptions, drawerIcons));


        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        loadPanels(FileIO.inProgressAssignments);
                        currentScreenIsInProgress = true;
                        break;
                    case 1:
                        loadPanels(FileIO.completedAssignments);
                        currentScreenIsInProgress = false;
                        break;
                    case 2:
                        startActivity(new Intent(MainActivity.this, TrashActivity.class));
                        break;
                    case 3:
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        break;
                }
                mDrawerLayout.closeDrawers();
            }
        });
    }

    private void initFAB() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setImageResource((ColorPicker.getColorSecondaryText() == Color.BLACK) ?
                R.drawable.ic_add_black_24dp : R.drawable.ic_add_white_24dp);
        fab.setBackgroundTintList(ColorStateList.valueOf(ColorPicker.getColorSecondary()));
        fab.setRippleColor(ColorPicker.getColorSecondary());
    }

    void initToolbar(boolean forInProgressAssignments) {
        if (forInProgressAssignments) {
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
        } else {
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
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final ArrayList<NewAssignment> assignments;
        if (getTitle().toString().equals(getResources().getString(R.string.header_completed)))
            assignments = FileIO.completedAssignments;
        else
            assignments = FileIO.inProgressAssignments;

//        loadPanels(assignments, currentSortIndex);

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
//
//            case R.id.action_open_classes:
//                startActivity(new Intent(this, ClassActivity.class));
//                return true;
//            case R.id.action_open_types:
//                return true;

            case R.id.action_delete_all:
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Are you sure you want to delete all assignments?");
                alertDialog.setMessage("They will be moved to trash and deleted after 30 days");
                alertDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FileIO.deleteAll(assignments, MainActivity.this);
                        loadPanels(assignments);
                    }
                });
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.create().show();
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    void loadPanels(ArrayList<NewAssignment> assignments) {
        loadPanels(assignments, 0);
    }


    public void loadPanels(ArrayList<NewAssignment> assignments, int sortIndex) {

        NotificationAlarms.setNotificationTimers(this);
        initToolbar(assignments == FileIO.inProgressAssignments);
        currentSortIndex = sortIndex;

        parent.removeAllViews();

        if (assignments.isEmpty()) {
            addHeading("There are no assignments.\nTo create a new assignment, press the +");
        } else {
            Comparator<NewAssignment> comparator;

            switch (sortIndex) {
                default:
                case 0: //sort by date
                    comparator = new Comparator<NewAssignment>() {
                        @Override
                        public int compare(NewAssignment o1, NewAssignment o2) {
                            return o1.dueDate.compareTo(o2.dueDate);
                        }
                    };
                    Collections.sort(assignments, comparator);
                    addViewsByDate(assignments);
                    break;
                case 1: //sort by class
                    comparator = new Comparator<NewAssignment>() {
                        @Override
                        public int compare(NewAssignment o1, NewAssignment o2) {
                            return o1.className.toUpperCase().compareTo(o2.className.toUpperCase());
                        }
                    };
                    Collections.sort(assignments, comparator);
                    addViewsByClass(assignments);
                    break;
                case 2: //sort by type
                    comparator = new Comparator<NewAssignment>() {
                        @Override
                        public int compare(NewAssignment o1, NewAssignment o2) {
                            return o1.type.toUpperCase().compareTo(o2.type.toUpperCase());
                        }
                    };
                    Collections.sort(assignments, comparator);
                    addViewsByType(assignments);
                    break;
                case 3: //sort by title
                    comparator = new Comparator<NewAssignment>() {
                        @Override
                        public int compare(NewAssignment o1, NewAssignment o2) {
                            return o1.title.toUpperCase().compareTo(o2.title.toUpperCase());
                        }
                    };
                    Collections.sort(assignments, comparator);
                    addViewsByTitle(assignments);
                    break;

            }
        }
        addHeading(" ");
        addHeading(" ");

    }

//    private ArrayList<RecyclerView> recyclerViews = new ArrayList<>();

    void addViewsByDate(ArrayList<NewAssignment> assignments) {
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DATE, 1);

        ArrayList<NewAssignment> priorityAssignments = new ArrayList<>();
        ArrayList<NewAssignment> overdueAssignments = new ArrayList<>();
        ArrayList<NewAssignment> everythingElse = new ArrayList<>();

        Log.v("MainActivity", "Overdue assignments:");
        for (NewAssignment assignment : assignments) {
            if (assignment.priority > 0) {
                priorityAssignments.add(assignment);
            } else if ((sharedPref.getBoolean(SettingsActivity.overdueLast, false)
                    && compareCalendars(assignment.dueDate, today) < 0)) {
                overdueAssignments.add(assignment);
                Log.v("MainActivity", assignment.toString());
            } else {
                everythingElse.add(assignment);
            }
        }
        Log.v("MainActivity", "");

        if (!priorityAssignments.isEmpty()) {
            addHeading("Priority");
            RecyclerView recyclerView = createRecyclerViewForList(priorityAssignments);

            parent.addView(recyclerView);
//            recyclerViews.add(recyclerView);
        }

        if (!everythingElse.isEmpty()) {
            NewAssignment previous = null; //assignments.get(0);

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US);
            ArrayList<NewAssignment> currentGroup = new ArrayList<>();

            for (NewAssignment assignment : everythingElse) {
                if (previous == null) {
                    addDateHeading(dateFormat, today, tomorrow, assignment.dueDate);
                    currentGroup = new ArrayList<>();
                } else if (compareCalendars(assignment.dueDate, previous.dueDate) > 0) {
                    RecyclerView recyclerView = createRecyclerViewForList(currentGroup);
                    parent.addView(recyclerView);
//                    recyclerViews.add(recyclerView);

                    addDateHeading(dateFormat, today, tomorrow, assignment.dueDate);
                    currentGroup = new ArrayList<>();
                }

                previous = assignment;

                currentGroup.add(assignment);
            }

            if (!currentGroup.isEmpty()) {
                RecyclerView recyclerView = createRecyclerViewForList(currentGroup);
                parent.addView(recyclerView);
//                recyclerViews.add(recyclerView);
            }
        }

        if (!overdueAssignments.isEmpty()) {
            addHeading("Overdue");
            RecyclerView recyclerView = createRecyclerViewForList(overdueAssignments);

            parent.addView(recyclerView);
//            recyclerViews.add(recyclerView);
        }
    }

    void addViewsByClass(ArrayList<NewAssignment> assignments) {
        ArrayList<NewAssignment> group = new ArrayList<>();

        String last = null;
        for (NewAssignment a : assignments) {
            if (!a.className.toUpperCase().equals(last)) {
                if (last != null) {
                    RecyclerView rV = createRecyclerViewForList(group);
                    parent.addView(rV);
                }
                addHeading(a.className.toUpperCase());
                group = new ArrayList<>();
            }
            group.add(a);
            last = a.className.toUpperCase();
        }
        if (!group.isEmpty()) {
            RecyclerView rV = createRecyclerViewForList(group);
            parent.addView(rV);
        }
    }

    void addViewsByType(ArrayList<NewAssignment> assignments) {
        ArrayList<NewAssignment> group = new ArrayList<>();

        String last = null;
        for (NewAssignment a : assignments) {
            if (!a.type.toUpperCase().equals(last)) {
                if (last != null) {
                    RecyclerView rV = createRecyclerViewForList(group);
                    parent.addView(rV);
                }
                addHeading(a.type.toUpperCase());
                group = new ArrayList<>();
            }
            group.add(a);
            last = a.type.toUpperCase();
        }
        if (!group.isEmpty()) {
            RecyclerView rV = createRecyclerViewForList(group);
            parent.addView(rV);
        }
    }

    void addViewsByTitle(ArrayList<NewAssignment> assignments) {
        ArrayList<NewAssignment> group = new ArrayList<>();

        Character last = null;
        for (NewAssignment a : assignments) {
            if (last == null) {
                addHeading(Character.toString(a.title.toUpperCase().charAt(0)));
                group = new ArrayList<>();
            } else if (a.title.toUpperCase().charAt(0) != last) {
                RecyclerView rV = createRecyclerViewForList(group);
                parent.addView(rV);
                addHeading(Character.toString(a.title.toUpperCase().charAt(0)));
                group = new ArrayList<>();
            }
            group.add(a);
            last = a.title.toUpperCase().charAt(0);
        }
        if (!group.isEmpty()) {
            RecyclerView rV = createRecyclerViewForList(group);
            parent.addView(rV);
        }
    }

    RecyclerView createRecyclerViewForList(ArrayList<NewAssignment> assignments) {
        final RecyclerView recyclerView = new RecyclerView(this);
//        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);
        recyclerView.setLayoutParams(params);

        adapter = new AssignmentItemAdapter(assignments, currentSortIndex, this);
        recyclerView.setAdapter(adapter);

        recyclerView.setNestedScrollingEnabled(false);

        SwipeCallback swipeCallback = new SwipeCallback(this) {

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            }

            @Override
            public void onSwiped(@Nullable RecyclerView.ViewHolder viewHolder, int direction) {
                AssignmentItemAdapter adapter = (AssignmentItemAdapter) recyclerView.getAdapter();
                assert viewHolder != null;
                if (direction == ItemTouchHelper.LEFT) {
                    adapter.removeAt(viewHolder.getAdapterPosition());
                } else if (direction == ItemTouchHelper.RIGHT) {
                    adapter.toggleDone(viewHolder.getAdapterPosition());
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return recyclerView;
    }

    void addHeading(int id) {
        addHeading(getString(id));
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
//
//    void sortViewsByClass(ArrayList<NewAssignment> assignments) {
//
//        ArrayList<String> headings = new ArrayList<>();
//        for (NewAssignment assignment : assignments) {
//            if (!headings.contains(assignment.className)) {
//                headings.add(assignment.className);
//            }
//        }
//        Collections.sort(headings);
//        for (String heading : headings) {
//            addHeading(heading);
//            for (NewAssignment assignment : assignments) {
//                if (assignment.className.equals(heading)) {
//                    AssignmentViewWrapper view = new AssignmentViewWrapper(
//                            this, assignment, currentSortIndex);
//                    parent.addView(view.container);
//                }
//            }
//        }
//    }
//
//    void sortViewsByType(ArrayList<NewAssignment> assignments) {
//
//        String[] types = getResources().getStringArray(R.array.assignment_types_array);
//        Collections.sort(assignments);
//        for (String type : types) {
//            addHeading(type);
//            for (NewAssignment assignment : assignments) {
//                if (assignment.type.equals(type)) {
//                    AssignmentViewWrapper view = new AssignmentViewWrapper(
//                            this, assignment, currentSortIndex);
//                    parent.addView(view.container);
//                }
//            }
//        }
//    }
//
//    void sortViewsByTitle(ArrayList<NewAssignment> assignments) {
//        for (int i = 0; i < assignments.size(); i++) {
//            int pos = i;
//            for (int j = i; j < assignments.size(); j++) {
//                if (assignments.get(j).title.compareTo(assignments.get(pos).title) < 0) {
//                    pos = j;
//                }
//            }
//
//            NewAssignment min = assignments.get(pos);
//            assignments.set(pos, assignments.get(i));
//            assignments.set(i, min);
//        }
//
//        char currentLetter = 0;
//        boolean emptyLetter = true; //helps with checking that blank character is added only once
//        for (NewAssignment assignment : assignments) {
//            if (assignment.title.length() == 0) {
//                if (emptyLetter) {
//                    addHeading("Untitled");
//                    emptyLetter = false;
//                }
//            } else if (assignment.title.toUpperCase().charAt(0) > currentLetter) {
//                currentLetter = assignment.title.charAt(0);
//                addHeading(Character.toString(currentLetter).toUpperCase());
//            }
//            AssignmentViewWrapper viewContainer = new AssignmentViewWrapper(
//                    this, assignment, currentSortIndex
//            );
//            parent.addView(viewContainer.container);
//        }
//
//    }

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