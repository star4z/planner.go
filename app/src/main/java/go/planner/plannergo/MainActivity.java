package go.planner.plannergo;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ColorSchemeActivity {

    private static final String TAG = "MainActivity";

    //Settings data
    private SharedPreferences sharedPref;
    private int currentSortIndex = 0;
    private FloatingActionButton fab;
    private boolean currentScreenIsInProgress = true;
    private ColorScheme colorScheme;

    //Quick references
    private LinearLayout parent;
    private Toolbar myToolbar;
    private boolean schemeSet = false;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private DrawerAdapter mDrawerAdapter;

    // drawer indices
    static final int iHeader = 0;
    static final int iInProgress = 1;
    static final int iCompleted = 2;
    static final int iTrash = 3;
    static final int iSettings = 4;
    static final int iFeedback = 5;

    /**
     * Runs the first time this instance of the activity becomes active
     *
     * @param savedInstanceState stores state for restoring the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        setColorScheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkFirstRun();


        parent = findViewById(R.id.parent);
        myToolbar = findViewById(R.id.toolbar);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        fab = findViewById(R.id.fab);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        setSupportActionBar(myToolbar);
    }


    /**
     * Runs every time the activity becomes active
     */
    @Override
    protected void onResume() {
        //Update data
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        FileIO.readFiles(this);

        checkForColorSchemeUpdate();

        //Call GUI setup methods
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
            boolean cTemp = currentScreenIsInProgress;
            currentScreenIsInProgress = intent.getExtras().getBoolean("mode_InProgress", true);
            if (cTemp != currentScreenIsInProgress)
                loadPanels((currentScreenIsInProgress) ?
                        FileIO.inProgressAssignments : FileIO.completedAssignments);
        }

        super.onNewIntent(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final ArrayList<Assignment> assignments;
        if (getTitle().toString().equals(getResources().getString(R.string.header_completed)))
            assignments = FileIO.completedAssignments;
        else
            assignments = FileIO.inProgressAssignments;


        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
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
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this,
                        colorScheme.equals(ColorScheme.Companion.getSCHEME_DARK()) ?
                                R.style.DarkDialogTheme :
                                R.style.LightDialogTheme);
                alertDialog.setTitle(R.string.delete_all);
                alertDialog.setMessage(R.string.move_to_trash_note);
                alertDialog.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FileIO.deleteAll(assignments, MainActivity.this);
                        loadPanels(assignments);
                    }
                });
                alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.create().show();
            default:
                return super.onOptionsItemSelected(item);

        }
    }


    @NonNull
    @Override
    public ColorScheme getColorScheme() {
        return colorScheme;
    }

    @Override
    public void setColorScheme() {
        boolean useDarkMode = sharedPref.getBoolean(Settings.darkMode, true);
        colorScheme = useDarkMode ? ColorScheme.Companion.getSCHEME_DARK() : ColorScheme.Companion.getSCHEME_LIGHT();
//        setTheme(colorScheme.getTheme());
        Log.d(TAG, "darkmode=" + useDarkMode);
    }

    @Override
    public void checkForColorSchemeUpdate() {
        boolean useDarkMode = sharedPref.getBoolean(Settings.darkMode, true);
        ColorScheme newScheme = useDarkMode ? ColorScheme.Companion.getSCHEME_DARK() : ColorScheme.Companion.getSCHEME_LIGHT();
        if (newScheme != colorScheme) {
            recreate();
        } else if (!schemeSet) {
            applyColors();
        }
    }

    @Override
    public void applyColors() {
        fab.setBackgroundTintList(ColorStateList.valueOf(colorScheme.getColor(this, Field.MAIN_BUTTON_BG)));
        NavigationView navView = findViewById(R.id.navigation);
        navView.setBackgroundColor(colorScheme.getColor(this, Field.DW_BG));
        CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinator);
        coordinatorLayout.setBackgroundColor(colorScheme.getColor(this, Field.MAIN_BG));
        schemeSet = true;
    }

    private void checkFirstRun() {
        File file = new File(getFilesDir(), "planner.info");
        try {
            if (file.createNewFile()) {
                startActivity(new Intent(this, TutorialActivityTitle.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initNavDrawer() {
        int StatusBarHeight = (int) Math.floor(25 * getResources().getDisplayMetrics().density);
        Log.d(TAG, "StatusBarHeight=" + StatusBarHeight);

        String[] drawerOptions = getResources().getStringArray(R.array.drawer_options_array);
        TypedArray tArray = getResources().obtainTypedArray(R.array.drawer_icons_array);
        int[] drawerIcons = new int[tArray.length()];
        for (int i = 0; i < drawerIcons.length; i++) {
            drawerIcons[i] = tArray.getResourceId(i, 0);
        }
        //Recycles the TypedArray, to be re-used by a later caller.
        //After calling this function you must not ever touch the typed array again.
        tArray.recycle();

        mDrawerList = findViewById(R.id.drawer_list);

        int selectedPos = currentScreenIsInProgress ? 1 : 2;

        mDrawerAdapter = new DrawerAdapter(this, drawerOptions, drawerIcons, selectedPos);
        mDrawerList.setAdapter(mDrawerAdapter);


        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case iInProgress:
                        loadPanels(FileIO.inProgressAssignments);
                        currentScreenIsInProgress = true;
                        break;
                    case iCompleted:
                        loadPanels(FileIO.completedAssignments);
                        currentScreenIsInProgress = false;
                        break;
                    case iTrash:
                        mDrawerAdapter.setSelectedPos(3);
                        mDrawerList.setAdapter(mDrawerAdapter);
                        startActivity(new Intent(MainActivity.this, TrashActivity.class));
                        overridePendingTransition(0, 0);
                        break;
                    case iSettings:
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        break;
                    case iFeedback:
                        startActivity(new Intent(MainActivity.this, FeedbackActivity.class));
                }
                mDrawerLayout.closeDrawers();
            }
        });
    }


    private void initToolbar(boolean forInProgressAssignments) {
        if (forInProgressAssignments) {
            setTitle(getResources().getString(R.string.header_in_progress));
            myToolbar.setBackgroundColor(colorScheme.getColor(this, Field.IP_APP_BAR_BG));
            myToolbar.setNavigationIcon(colorScheme.getDrawable(this, Field.IP_APP_BAR_HAM));
            myToolbar.setTitleTextColor(colorScheme.getColor(this, Field.IP_APP_BAR_TEXT));
            myToolbar.setOverflowIcon(colorScheme.getDrawable(this, Field.IP_APP_BAR_OPT));
        } else {
            setTitle(getResources().getString(R.string.header_completed));
            myToolbar.setBackgroundColor(colorScheme.getColor(this, Field.CP_APP_BAR_BG));
            myToolbar.setNavigationIcon(colorScheme.getDrawable(this, Field.CP_APP_BAR_HAM));
            myToolbar.setTitleTextColor(colorScheme.getColor(this, Field.CP_APP_BAR_TEXT));
            myToolbar.setOverflowIcon(colorScheme.getDrawable(this, Field.CP_APP_BAR_OPT));
        }
    }


    void loadPanels(ArrayList<Assignment> assignments) {
        loadPanels(assignments, 0);
    }


    void loadPanels(ArrayList<Assignment> assignments, int sortIndex) {
        if (sharedPref.getBoolean(Settings.notifEnabled, true))
            NotificationAlarms.setNotificationTimers(this);
        currentScreenIsInProgress = assignments == FileIO.inProgressAssignments;
        initToolbar(currentScreenIsInProgress);
        mDrawerAdapter.setSelectedPos((currentScreenIsInProgress) ? 1 : 2);
        mDrawerList.setAdapter(mDrawerAdapter);
        currentSortIndex = sortIndex;

        parent.removeAllViews();

        if (assignments.isEmpty()) {
            addHeading(R.string.empty_assignment_set);
        } else {
            Comparator<Assignment> comparator;

            switch (sortIndex) {
                default:
                case 0: //sort by date
                    comparator = new Comparator<Assignment>() {
                        @Override
                        public int compare(Assignment o1, Assignment o2) {
                            return o1.dueDate.compareTo(o2.dueDate);
                        }
                    };
                    Collections.sort(assignments, comparator);
                    addViewsByDate(assignments);
                    break;
                case 1: //sort by class
                    comparator = new Comparator<Assignment>() {
                        @Override
                        public int compare(Assignment o1, Assignment o2) {
                            return o1.className.toUpperCase().compareTo(o2.className.toUpperCase());
                        }
                    };
                    Collections.sort(assignments, comparator);
                    addViewsByClass(assignments);
                    break;
                case 2: //sort by type
                    comparator = new Comparator<Assignment>() {
                        @Override
                        public int compare(Assignment o1, Assignment o2) {
                            return o1.type.toUpperCase().compareTo(o2.type.toUpperCase());
                        }
                    };
                    Collections.sort(assignments, comparator);
                    addViewsByType(assignments);
                    break;
                case 3: //sort by title
                    comparator = new Comparator<Assignment>() {
                        @Override
                        public int compare(Assignment o1, Assignment o2) {
                            return o1.title.toUpperCase().compareTo(o2.title.toUpperCase());
                        }
                    };
                    Collections.sort(assignments, comparator);
                    addViewsByTitle(assignments);
                    break;

            }
        }
    }


    private void addViewsByDate(ArrayList<Assignment> assignments) {
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DATE, 1);

        ArrayList<Assignment> priorityAssignments = new ArrayList<>();
        ArrayList<Assignment> overdueAssignments = new ArrayList<>();
        ArrayList<Assignment> everythingElse = new ArrayList<>();

        for (Assignment assignment : assignments) {
            if (assignment.priority > 0) {
                priorityAssignments.add(assignment);
            } else if ((sharedPref.getBoolean(Settings.overdueLast, false)
                    && compareCalendars(assignment.dueDate, today) < 0)) {
                overdueAssignments.add(assignment);
            } else {
                everythingElse.add(assignment);
            }
        }

        if (!priorityAssignments.isEmpty()) {
            final TextView heading = addHeading(R.string.priority);
            final RecyclerView recyclerView = createRecyclerViewForList(priorityAssignments, heading);

            parent.addView(recyclerView);
        }

        if (!everythingElse.isEmpty()) {
            Assignment previous = null;

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US);
            ArrayList<Assignment> currentGroup = new ArrayList<>();

            TextView lastHeading = null;
            for (Assignment assignment : everythingElse) {
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
            TextView heading = addHeading(R.string.due_overdue);
            RecyclerView recyclerView = createRecyclerViewForList(overdueAssignments, heading);
            parent.addView(recyclerView);
        }
    }

    private void addViewsByClass(ArrayList<Assignment> assignments) {
        ArrayList<Assignment> group = new ArrayList<>();

        String last = null;
        TextView lastHeading = null;
        for (Assignment a : assignments) {
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

    private void addViewsByType(ArrayList<Assignment> assignments) {
        ArrayList<Assignment> group = new ArrayList<>();

        String last = null;
        TextView lastHeading = null;
        for (Assignment a : assignments) {
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

    private void addViewsByTitle(ArrayList<Assignment> assignments) {
        ArrayList<Assignment> group = new ArrayList<>();

        String last = null;
        TextView lastHeading = null;
        for (Assignment a : assignments) {
            if (last == null) {
                lastHeading = (a.title.length() > 0)
                        ? addHeading(Character.toString(a.title.toUpperCase().charAt(0)))
                        : addHeading(R.string.untitled);
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

    private RecyclerView createRecyclerViewForList(ArrayList<Assignment> assignments, final TextView heading) {
        final RecyclerView recyclerView = new RecyclerView(this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);
        recyclerView.setLayoutParams(params);

        RecyclerView.Adapter adapter = new AssignmentItemAdapter(assignments, currentSortIndex, this);
        recyclerView.setAdapter(adapter);

        recyclerView.setNestedScrollingEnabled(false);

        SwipeCallback swipeCallback = new SwipeCallback(this) {
            @SuppressWarnings("NullableProblems")
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            }

            @Override
            public void onSwiped(@Nullable RecyclerView.ViewHolder viewHolder, int direction) {
                AssignmentItemAdapter adapter = (AssignmentItemAdapter) recyclerView.getAdapter();
                assert viewHolder != null;
                if (direction == ItemTouchHelper.LEFT) {
                    assert adapter != null;
                    adapter.removeAt(viewHolder.getAdapterPosition());
                } else if (direction == ItemTouchHelper.RIGHT) {
                    assert adapter != null;
                    adapter.toggleDone(viewHolder.getAdapterPosition());
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @SuppressWarnings("NullableProblems")
            @Override
            public void onChildViewAttachedToWindow(View view) {
                if (heading.getVisibility() != View.VISIBLE)
                    heading.setVisibility(View.VISIBLE);
            }

            @SuppressWarnings("NullableProblems")
            @Override
            public void onChildViewDetachedFromWindow(View view) {
                int count = 0;
                if (recyclerView.getAdapter() != null) {
                    count = recyclerView.getAdapter().getItemCount();
                }
                Log.v(TAG, "count=" + count);
                if (count <= 0) {
                    heading.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                }
            }
        });

        return recyclerView;
    }

    private TextView addHeading(int id) {
        return addHeading(getString(id));
    }

    private TextView addHeading(String text) {
        TextView header = (TextView) getLayoutInflater().inflate(
                R.layout.view_sort_header,
                (ViewGroup) findViewById(android.R.id.content),
                false
        );
        header.setText((text.equals("") ? getString(R.string.untitled) : text));
        header.setTextColor(colorScheme.getColor(this, Field.MAIN_HEADER));
        parent.addView(header);
        return header;
    }

    private TextView addDateHeading(SimpleDateFormat dateFormat, Calendar today, Calendar tomorrow, Calendar date) {
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

    private int compareCalendars(Calendar c1, Calendar c2) {
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
    public void createNew(View view) {
        startActivity(new Intent(MainActivity.this, NewAssignmentActivity.class));
    }
} // end MainActivity class