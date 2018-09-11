package go.planner.plannergo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity implements SettingsFragment.Callback {

    public static final String timeEnabled = "pref_time_enabled";
    public static final String overdueLast = "pref_overdue_last";

    private static final String TAG_NESTED = "TAG_NESTED";

    private Toolbar toolbar;

    public boolean listStyled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_prefs);


        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        toolbar.setNavigationContentDescription(R.string.back);
        if (ColorPicker.getColorSecondaryText() == Color.BLACK) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        }
        setSupportActionBar(toolbar);



        if (savedInstanceState == null) {
            // Display the fragment
            getFragmentManager().beginTransaction()
                    .replace(R.id.parent, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        ColorPicker.setColors(this);
        toolbar.setBackgroundColor(ColorPicker.getColorSecondary());
        toolbar.setTitleTextColor(ColorPicker.getColorSecondaryText());
        getWindow().setStatusBarColor(ColorPicker.getColorSecondaryAccent());
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        Log.v("SettingsActivity", "backStackEntryCount=" + getFragmentManager().getBackStackEntryCount());
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            toolbar.setTitle(R.string.settings);
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if (getFragmentManager().getBackStackEntryCount() == 0) {
                    NavUtils.navigateUpFromSameTask(this);
                } else {
                    toolbar.setTitle(R.string.set);
                    getFragmentManager().popBackStack();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNestedPreferenceSelected(int key) {
        //if multiple nested preference screens are made, this needs to be rewritten
        toolbar.setTitle(R.string.notifications);

        getFragmentManager().beginTransaction().replace(
                R.id.parent,
                NestedPreferencesFragment.newInstance(key),
                TAG_NESTED).
                addToBackStack(TAG_NESTED).
                commit();

    }

}
