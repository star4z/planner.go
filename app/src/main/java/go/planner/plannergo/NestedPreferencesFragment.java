package go.planner.plannergo;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

/**
 * Handles creation of preference sub-menus
 * Created by bdphi on 3/15/2018.
 */

@SuppressWarnings("deprecation")
public class NestedPreferencesFragment extends PreferenceFragment {

    static final int KEY_NOTIFY = 1;

    private static final String TAG_KEY = "NESTED_KEY";

    static NestedPreferencesFragment newInstance(int key) {
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

        changeTextColors();
    }


    private void checkPreferenceResource() {
        int key = getArguments().getInt(TAG_KEY);
        // Load the preferences from an XML resource
        if (key == KEY_NOTIFY) {
            addPreferencesFromResource(R.xml.notification_preferences);
        }
    }

    private void changeTextColors() {
        if (getActivity() instanceof ColorSchemeActivity) {
            changeTextColors(getPreferenceScreen(), (ColorSchemeActivity) getActivity());
        }
    }

    private void changeTextColors(PreferenceGroup group, ColorSchemeActivity activity) {
        for (int i = 0; i < group.getPreferenceCount(); i++) {
            Preference preference = group.getPreference(i);

            if (preference instanceof PreferenceGroup)
                changeTextColors((PreferenceGroup) preference, activity);
            else {
                if (activity.getColorScheme().equals(ColorScheme.SCHEME_LIGHT))
                    preference.setLayoutResource(R.layout.preference);

                else
                    preference.setLayoutResource(R.layout.preference_dark);

            }
        }
    }
}


