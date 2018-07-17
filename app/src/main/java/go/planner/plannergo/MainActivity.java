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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import go.planner.plannergo.billing.BillingManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

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

    //Billing
    private MainViewController mViewController;
    private BillingManager mBillingManager;
//    private AcquireFragment mAcquireFragment;
    /**
     * Runs when the activity is created
     *
     * @param savedInstanceState stores state for restoring the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkFirstRun();

        parent = findViewById(R.id.parent);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        mViewController = new MainViewController(this);
        mBillingManager = new BillingManager(this, mViewController.getUpdateListener());
    }


    /**
     * Runs when the activity becomes active (again)
     */
    @Override
    protected void onResume() {
        //Update data
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ColorPicker.setColors(this);
        FileIO.readFiles(this);

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
        Log.v(TAG, "NewIntent");
        if (intent.getExtras() != null) {
            long idToRemove = intent.getExtras().getLong("remove_id", -1);
            Log.v(TAG, "idToRemove=" + idToRemove);
            if (idToRemove != -1) {
                FileIO.deleteAssignment(this, FileIO.getAssignment(idToRemove));
            }

            currentScreenIsInProgress = intent.getExtras().getBoolean("mode_InProgress", true);
        }

        super.onNewIntent(intent);
    }

    private void checkFirstRun(){
        File file = new File(getFilesDir(), "planner.info");
        try {
            if (file.createNewFile()) {
                startActivity(new Intent(this, TutorialActivity.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

            case R.id.action_open_classes:
                startActivity(new Intent(this, ClassActivity.class));
                return true;
            case R.id.action_open_types:
                startActivity(new Intent(this, TypeActivity.class));
                return true;

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


    void addViewsByDate(ArrayList<NewAssignment> assignments) {
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DATE, 1);

        ArrayList<NewAssignment> priorityAssignments = new ArrayList<>();
        ArrayList<NewAssignment> overdueAssignments = new ArrayList<>();
        ArrayList<NewAssignment> everythingElse = new ArrayList<>();

        for (NewAssignment assignment : assignments) {
            if (assignment.priority > 0) {
                priorityAssignments.add(assignment);
            } else if ((sharedPref.getBoolean(SettingsActivity.overdueLast, false)
                    && compareCalendars(assignment.dueDate, today) < 0)) {
                overdueAssignments.add(assignment);
            } else {
                everythingElse.add(assignment);
            }
        }

        if (!priorityAssignments.isEmpty()) {
            final TextView heading = addHeading("Priority");
            final RecyclerView recyclerView = createRecyclerViewForList(priorityAssignments, heading);

            parent.addView(recyclerView);
        }

        if (!everythingElse.isEmpty()) {
            NewAssignment previous = null;

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US);
            ArrayList<NewAssignment> currentGroup = new ArrayList<>();

            TextView lastHeading = null;
            for (NewAssignment assignment : everythingElse) {
                if (previous == null) {
                    lastHeading = addDateHeading(dateFormat, today, tomorrow, assignment.dueDate);
                    currentGroup = new ArrayList<>();
                } else if (compareCalendars(assignment.dueDate, previous.dueDate) > 0) {
                    RecyclerView recyclerView = createRecyclerViewForList(currentGroup, lastHeading);
                    parent.addView(recyclerView);

                    lastHeading = addDateHeading(dateFormat, today, tomorrow, assignment.dueDate);
                    currentGroup = new ArrayList<>();
                }

                previous = assignment;

                currentGroup.add(assignment);
            }

            if (!currentGroup.isEmpty()) {
                RecyclerView recyclerView = createRecyclerViewForList(currentGroup, lastHeading);
                parent.addView(recyclerView);
            }
        }

        if (!overdueAssignments.isEmpty()) {
            TextView heading = addHeading("Overdue");
            RecyclerView recyclerView = createRecyclerViewForList(overdueAssignments, heading);
            parent.addView(recyclerView);
        }
    }

    void addViewsByClass(ArrayList<NewAssignment> assignments) {
        ArrayList<NewAssignment> group = new ArrayList<>();

        String last = null;
        TextView lastHeading = null;
        for (NewAssignment a : assignments) {
            if (!a.className.toUpperCase().equals(last)) {
                if (last != null) {
                    RecyclerView rV = createRecyclerViewForList(group, lastHeading);
                    parent.addView(rV);
                }
                lastHeading = addHeading(a.className.toUpperCase());
                group = new ArrayList<>();
            }
            group.add(a);
            last = a.className.toUpperCase();
        }
        if (!group.isEmpty()) {
            RecyclerView rV = createRecyclerViewForList(group, lastHeading);
            parent.addView(rV);
        }
    }

    void addViewsByType(ArrayList<NewAssignment> assignments) {
        ArrayList<NewAssignment> group = new ArrayList<>();

        String last = null;
        TextView lastHeading = null;
        for (NewAssignment a : assignments) {
            if (!a.type.toUpperCase().equals(last)) {
                if (last != null) {
                    RecyclerView rV = createRecyclerViewForList(group, lastHeading);
                    parent.addView(rV);
                }
                lastHeading = addHeading(a.type.toUpperCase());
                group = new ArrayList<>();
            }
            group.add(a);
            last = a.type.toUpperCase();
        }
        if (!group.isEmpty()) {
            RecyclerView rV = createRecyclerViewForList(group, lastHeading);
            parent.addView(rV);
        }
    }

    void addViewsByTitle(ArrayList<NewAssignment> assignments) {
        ArrayList<NewAssignment> group = new ArrayList<>();

        String last = null;
        TextView lastHeading = null;
        for (NewAssignment a : assignments) {
            if (last == null) {
                lastHeading = (a.title.length() > 0)
                        ? addHeading(Character.toString(a.title.toUpperCase().charAt(0)))
                        : addHeading("Untitled");
                group = new ArrayList<>();
            } else if (a.title.length() > 0 && !Character.toString(a.title.toUpperCase().charAt(0)).equals(last)) {
                RecyclerView rV = createRecyclerViewForList(group, lastHeading);
                parent.addView(rV);
                addHeading(Character.toString(a.title.toUpperCase().charAt(0)));
                group = new ArrayList<>();
            }
            group.add(a);
            last = (a.title.length() > 0)
                    ? Character.toString(a.title.toUpperCase().charAt(0))
                    : a.title;
        }
        if (!group.isEmpty()) {
            RecyclerView rV = createRecyclerViewForList(group, lastHeading);
            parent.addView(rV);
        }
    }

    RecyclerView createRecyclerViewForList(ArrayList<NewAssignment> assignments, final TextView heading) {
        final RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setHasFixedSize(true);

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

        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                if (heading.getVisibility() != View.VISIBLE) {
                    heading.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                int count = 0;
                if (recyclerView.getAdapter() != null) {
                    count = recyclerView.getAdapter().getItemCount();
                }
                Log.v(TAG, "count=" + count);
                if (count <= 0) {
                    heading.setVisibility(View.GONE);
                }
            }
        });

        return recyclerView;
    }

    TextView addHeading(int id) {
        return addHeading(getString(id));
    }

    TextView addHeading(String text) {
        if (text.equals(""))
            text = "Untitled";
        TextView header = (TextView) getLayoutInflater().inflate(
                R.layout.view_sort_header,
                (ViewGroup) findViewById(android.R.id.content),
                false
        );
        header.setText(text);
        parent.addView(header);
        return header;
    }

    TextView addDateHeading(SimpleDateFormat dateFormat, Calendar today, Calendar tomorrow, Calendar date) {
        int compareToToday = compareCalendars(date, today);
        int compareToTomorrow = compareCalendars(date, tomorrow);
        if (compareToToday == 0) {
            return addHeading(R.string.due_today);
        } else if (compareToTomorrow == 0) {
            return addHeading(R.string.due_tomorrow);
        } else {
            return addHeading(dateFormat.format(date.getTime()));
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
    //TODO: pop-up with extra options, define classes, class colors (currently elsewhere)
    public void createNew(View view) {
        startActivity(new Intent(MainActivity.this, NewAssignmentActivity.class));
    }
} // end MainActivity class