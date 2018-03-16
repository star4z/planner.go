package go.planner.plannergo;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Handles creation of preference sub-menus
 * Created by bdphi on 3/15/2018.
 */

public class NestedPreferencesFragment extends PreferenceFragment  {

    public static final int KEY_NOTIFY = 1;

    private static final String TAG_KEY = "NESTED_KEY";

    public static NestedPreferencesFragment newInstance(int key) {
        NestedPreferencesFragment fragment = new NestedPreferencesFragment();
        // supply arguments to bundle.
        Bundle args = new Bundle();
        args.putInt(TAG_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        checkPreferenceResource();
    }


    private void checkPreferenceResource() {
        int key = getArguments().getInt(TAG_KEY);
        // Load the preferences from an XML resource
        switch (key) {
            case KEY_NOTIFY:
                addPreferencesFromResource(R.xml.notification_preferences);
                break;

            default:
                break;
        }
    }
}
