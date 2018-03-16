package go.planner.plannergo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity implements SettingsFragment.Callback {

    public static final String defaultSort = "pref_default_sort";
    public static final String timeEnabled = "pref_time_enabled";
    public static final String overdueLast = "pref_overdue_last";

    private static final String TAG_NESTED = "TAG_NESTED";

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_prefs);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.p1_secondary));
        getWindow().setStatusBarColor(getResources().getColor(R.color.p1_secondary_dark));


        if (savedInstanceState == null) {
            // Display the fragment
            getFragmentManager().beginTransaction()
                    .replace(R.id.body, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        Log.v("SettingsActivity","backStackEntryCount=" + getFragmentManager().getBackStackEntryCount());
        if(getFragmentManager().getBackStackEntryCount()==0){
            super.onBackPressed();
        } else {
            toolbar.setTitle("Settings");
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if(getFragmentManager().getBackStackEntryCount()==0){
                    NavUtils.navigateUpFromSameTask(this);
                } else {
                    toolbar.setTitle("Settings");
                    getFragmentManager().popBackStack();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNestedPreferenceSelected(int key) {
        //if multiple nested preference screens are made, this needs to be rewritten
        toolbar.setTitle("Notifications");
        getFragmentManager().beginTransaction().replace(
                R.id.body,
                NestedPreferencesFragment.newInstance(key),
                TAG_NESTED).
                addToBackStack(TAG_NESTED).
                commit();
    }

    public static int getInt(String sort, Context context) {
        String[] sortOptions = context.getResources().getStringArray(R.array.sort_type_values);
        for (int i = 0; i < sortOptions.length; i++) {
            if (sort.equals(sortOptions[i])) {
                return i;
            }
        }
        return -1;
    }


}
