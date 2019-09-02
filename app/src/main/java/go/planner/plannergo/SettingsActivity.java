package go.planner.plannergo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity implements SettingsFragment.Callback, ColorSchemeActivity {
    private static final String TAG = "SettingsActivity";

    private static final String TAG_NESTED = "TAG_NESTED";

    private Toolbar toolbar;
    private LinearLayout parent;

    private SharedPreferences prefs;
    private ColorScheme colorScheme;
    private boolean schemeSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setColorScheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefs);

        parent = findViewById(R.id.parent);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        toolbar.setNavigationContentDescription(R.string.back);
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
        checkForColorSchemeUpdate();
        super.onResume();
    }

    /**
     * Respond to user's Back action
     */
    @Override
    public void onBackPressed() {
        goBack();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                goBack();
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

    private void goBack() {
        Log.d(TAG, "backStackEntryCount=" + getFragmentManager().getBackStackEntryCount());
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            toolbar.setTitle(R.string.settings);
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public void setColorScheme() {
        boolean scheme = prefs.getBoolean(Settings.darkMode, true);
        colorScheme = new ColorScheme(scheme, this);
        if (colorScheme.equals(ColorScheme.SCHEME_DARK))
            setTheme(R.style.DarkTheme_Fade);
        else
            setTheme(R.style.LightTheme_Fade);
        Log.d(TAG, "scheme=" + scheme);
    }

    @NotNull
    @Override
    public ColorScheme getColorScheme() {
        return colorScheme;
    }

    @Override
    public void checkForColorSchemeUpdate() {
        boolean isDarkMode = prefs.getBoolean(Settings.darkMode, true);
        ColorScheme newScheme = new ColorScheme(isDarkMode, this);
        if (!newScheme.equals(colorScheme))
            recreate();
        else if (!schemeSet)
            applyColors();
    }

    @Override
    public void applyColors() {
        toolbar.setBackgroundColor(colorScheme.getColor(ColorScheme.PRIMARY));
        toolbar.setTitleTextColor(colorScheme.getColor(ColorScheme.TEXT_COLOR));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        Objects.requireNonNull(toolbar.getNavigationIcon()).setTint(colorScheme.getColor(ColorScheme.TEXT_COLOR));
        parent.setBackgroundColor(colorScheme.getColor(ColorScheme.PRIMARY));
        schemeSet = true;
    }
}
